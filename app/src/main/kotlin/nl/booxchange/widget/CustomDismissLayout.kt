package nl.booxchange.widget

import android.content.Context
import android.support.v4.view.*
import android.support.v4.widget.ListViewCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.AbsListView
import android.widget.ListView
import org.jetbrains.anko.childrenSequence
import org.jetbrains.anko.dip
import kotlin.math.sign
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty2

class CustomDismissLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null): CardView(context, attrs), NestedScrollingParent, NestedScrollingChild {

    private val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var mTotalDragDistance = -1f

    private var mTotalUnconsumed: Float = 0.toFloat()
    private val mNestedScrollingParentHelper: NestedScrollingParentHelper
    private val mNestedScrollingChildHelper: NestedScrollingChildHelper
    private val mParentScrollConsumed = IntArray(2)
    private val mParentOffsetInWindow = IntArray(2)
    private var mNestedScrollInProgress: Boolean = false

    private var mCurrentTargetOffsetTop: Int = 0

    private var mInitialMotionY = 0f
    private var mInitialDownY = 0f
    private var mIsBeingDragged = false
    private var mActivePointerId = INVALID_POINTER

    private var mReturningToStart = false
    private val mDecelerateInterpolator: DecelerateInterpolator

    private var mLoadingViewIndex = -1
    private var mProgressViewStartOffset: Int = 0
    private var mProgressViewEndOffset: Int = 0

    private var mNotify: Boolean = false
    private var mUsingCustomStart = false

    private var onDismissAction: (() -> Unit)? = null
    fun setOnDismissAction(dismissAction: (() -> Unit)? = null) {
        onDismissAction = dismissAction
    }

    private var mTargetView: View? = null
        get() = field ?: childrenSequence().first()

    init {
        setWillNotDraw(false)
        mDecelerateInterpolator = DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR)

        isChildrenDrawingOrderEnabled = true

        mProgressViewEndOffset = dip(50)
        mTotalDragDistance = mProgressViewEndOffset.toFloat()
        mNestedScrollingParentHelper = NestedScrollingParentHelper(this)
        mNestedScrollingChildHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true

        mCurrentTargetOffsetTop = 0
        mProgressViewStartOffset = mCurrentTargetOffsetTop
    }

    override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        return when {
            mLoadingViewIndex < 0 -> i
            i == childCount - 1 -> mLoadingViewIndex
            i >= mLoadingViewIndex -> i + 1
            else -> i
        }
    }

    private val canChildScrollUp
        get() = (mTargetView as? ListView)?.let { ListViewCompat.canScrollList(it, -1) } ?: mTargetView?.canScrollVertically(-1) ?: false

    private val canChildScrollDown
        get() = (mTargetView as? ListView)?.let { ListViewCompat.canScrollList(it, 1) } ?: mTargetView?.canScrollVertically(1) ?: false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked

        if (!isEnabled || mReturningToStart || (canChildScrollDown && canChildScrollUp) || mNestedScrollInProgress) return false

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mActivePointerId = ev.getPointerId(0)
                mIsBeingDragged = false

                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) return false
                mInitialDownY = ev.getY(pointerIndex)
            }

            MotionEvent.ACTION_MOVE -> {
                if (mActivePointerId == INVALID_POINTER) return false
                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) return false
                val y = ev.getY(pointerIndex)
                if (!canChildScrollUp) {
                    startNegativeDragging(y)
                }
                if (!canChildScrollDown) {
                    startPositiveDragging(y)
                }
            }

            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
            }
        }

        return mIsBeingDragged
    }

    override fun requestDisallowInterceptTouchEvent(b: Boolean) {
        if (!(android.os.Build.VERSION.SDK_INT < 21 && mTargetView is AbsListView || mTargetView?.let { !ViewCompat.isNestedScrollingEnabled(it) } == true)) {
            super.requestDisallowInterceptTouchEvent(b)
        }
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return (isEnabled && !mReturningToStart && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0)
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes)
        startNestedScroll(axes and ViewCompat.SCROLL_AXIS_VERTICAL)
        mTotalUnconsumed = 0f
        mNestedScrollInProgress = true
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        if (dy > 0 && mTotalUnconsumed > 0) {
            if (dy > mTotalUnconsumed) {
                consumed[1] = dy - mTotalUnconsumed.toInt()
                mTotalUnconsumed = 0f
            } else {
                mTotalUnconsumed -= dy.toFloat()
                consumed[1] = dy
            }
            processDragMotionDown(mTotalUnconsumed)
        }

        val parentConsumed = mParentScrollConsumed
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0]
            consumed[1] += parentConsumed[1]
        }
    }

    override fun getNestedScrollAxes(): Int {
        return mNestedScrollingParentHelper.nestedScrollAxes
    }

    override fun onStopNestedScroll(target: View) {
        mNestedScrollingParentHelper.onStopNestedScroll(target)
        mNestedScrollInProgress = false
        if (mTotalUnconsumed != 0f) {
            finishSpinner(mTotalUnconsumed)
            mTotalUnconsumed = 0f
        }
        stopNestedScroll()
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow)

        val dy = dyUnconsumed + mParentOffsetInWindow[1]
        if (dy < 0 && !canChildScrollUp) {
            mTotalUnconsumed += Math.abs(dy).toFloat()
            processDragMotionDown(mTotalUnconsumed)
        } else if (dy > 0 && !canChildScrollDown) {
            mTotalUnconsumed -= Math.abs(dy).toFloat()
            processDragMotionUp(mTotalUnconsumed)
        }
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mNestedScrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mNestedScrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mNestedScrollingChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    private fun processDragMotionDown(overscrollTop: Float) {
        val originalDragPercent = overscrollTop / mTotalDragDistance
        val dragPercent = Math.min(1f, Math.abs(originalDragPercent))
        val extraOS = Math.abs(overscrollTop) - mTotalDragDistance
        val slingshotDist = (if (mUsingCustomStart) mProgressViewEndOffset - mProgressViewStartOffset else mProgressViewEndOffset).toFloat()
        val tensionSlingshotPercent = Math.max(0f, Math.min(extraOS, slingshotDist * 2) / slingshotDist)
        val tensionPercent = (tensionSlingshotPercent / 4 - Math.pow((tensionSlingshotPercent / 4).toDouble(), 2.0)).toFloat() * 2f
        val extraMove = slingshotDist * tensionPercent * 2f
        val targetY = mProgressViewStartOffset + slingshotDist * dragPercent + extraMove
        translationY = overscrollTop
    }

    private fun processDragMotionUp(overscrollTop: Float) {
        val originalDragPercent = overscrollTop / mTotalDragDistance
        val dragPercent = Math.min(1f, Math.abs(originalDragPercent))
        val extraOS = Math.abs(overscrollTop) - mTotalDragDistance
        val slingshotDist = (if (mUsingCustomStart) mProgressViewEndOffset - mProgressViewStartOffset else mProgressViewEndOffset).toFloat()
        val tensionSlingshotPercent = Math.max(0f, Math.min(extraOS, slingshotDist * 2) / slingshotDist)
        val tensionPercent = (tensionSlingshotPercent / 4 - Math.pow((tensionSlingshotPercent / 4).toDouble(), 2.0)).toFloat() * 2f
        val extraMove = slingshotDist * tensionPercent * 2f
        val targetY = mProgressViewStartOffset + slingshotDist * dragPercent + extraMove
        translationY = overscrollTop
    }

    private fun finishSpinner(overscrollTop: Float) {
        if ((overscrollTop > mTotalDragDistance && translationY > 0) || (-overscrollTop > mTotalDragDistance && translationY < 0)) {
            animateOffsetToEndPosition()
        } else {
            animateOffsetToStartPosition()
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked

        if (!isEnabled || mReturningToStart || (canChildScrollDown && canChildScrollUp) || mNestedScrollInProgress) return false

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mActivePointerId = ev.getPointerId(0)
                mIsBeingDragged = false
            }

            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) return false

                val y = ev.getY(pointerIndex)
                if (!canChildScrollUp) {
                    startNegativeDragging(y)
                }
                if (!canChildScrollDown) {
                    startPositiveDragging(y)
                }

                if (mIsBeingDragged) {
                    val overscrollTop = (y - mInitialMotionY) * DRAG_RATE
                    when {
                        overscrollTop > 0 -> processDragMotionDown(overscrollTop)
                        overscrollTop < 0 -> processDragMotionUp(overscrollTop)
                        else -> return false
                    }
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerIndex = ev.actionIndex
                if (pointerIndex < 0) return false
                mActivePointerId = ev.getPointerId(pointerIndex)
            }

            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)

            MotionEvent.ACTION_UP -> {
                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) return false

                if (mIsBeingDragged) {
                    val y = ev.getY(pointerIndex)
                    val overscrollTop = (y - mInitialMotionY) * DRAG_RATE
                    mIsBeingDragged = false
                    finishSpinner(overscrollTop)
                }
                mActivePointerId = INVALID_POINTER
                return false
            }

            MotionEvent.ACTION_CANCEL -> return false
        }

        return true
    }

    private fun startNegativeDragging(y: Float) {
        val yDiff = y - mInitialDownY
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop
            mIsBeingDragged = true
        }
    }

    private fun startPositiveDragging(y: Float) {
        val yDiff = mInitialDownY - y
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop
            mIsBeingDragged = true
        }
    }

    private fun animateOffsetToEndPosition() {
        mReturningToStart = true
        animate()?.translationY(height * translationY.sign)?.setDuration(350L)?.setInterpolator(mDecelerateInterpolator)?.withEndAction { mReturningToStart = false; onDismissAction?.invoke() }?.start()
    }

    private fun animateOffsetToStartPosition() {
        mReturningToStart = true
        animate()?.translationY(0f)?.setDuration(250L)?.setInterpolator(mDecelerateInterpolator)?.withEndAction { mReturningToStart = false }?.start()
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.actionIndex
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    fun appear(direction: PopupDirection = PopupDirection.UP) {
        translationY = when (direction) {
            PopupDirection.UP -> height
            PopupDirection.DOWN -> -height
        }.toFloat()

        animateOffsetToStartPosition()
    }

    fun dismiss(direction: PopupDirection = PopupDirection.DOWN) {
        translationY = when (direction) {
            PopupDirection.UP -> -1f
            PopupDirection.DOWN -> 1f
        }.toFloat()

        animateOffsetToEndPosition()
    }

    enum class PopupDirection {
        UP, DOWN
    }

    companion object {
        private const val DECELERATE_INTERPOLATION_FACTOR = 2f
        private const val INVALID_POINTER = -1
        private const val DRAG_RATE = .5f
    }
}
