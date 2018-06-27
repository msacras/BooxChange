package nl.booxchange.widget

import android.content.Context
import android.support.v4.view.*
import android.support.v4.widget.ListViewCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.AbsListView
import android.widget.FrameLayout
import android.widget.ListView
import nl.booxchange.R
import nl.booxchange.extension.getColorCompat
import org.jetbrains.anko.childrenSequence
import org.jetbrains.anko.dip
import kotlin.math.absoluteValue
import kotlin.math.sign

class CustomRefreshLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null): FrameLayout(context, attrs), NestedScrollingParent, NestedScrollingChild {

    var canRefreshUp = false
    var canRefreshDown = true

    private var mLoadingView = View(context).apply {
        layoutParams = FrameLayout.LayoutParams(dip(75), dip(30), Gravity.CENTER_HORIZONTAL)
        background = LAnimationDrawable(loopsPerSecond = 1.5f, thickness = dip(2).toFloat(), width = dip(18).toFloat(), primaryColor = context.getColorCompat(R.color.colorPrimary), secondaryColor = context.getColorCompat(R.color.colorPrimaryDark))
        alpha = 0f
        translationY = (-dip(30)).toFloat()
    }

    private var mRefreshing = false
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

    private var onUpRefreshAction: (() -> Unit)? = null
    fun setOnUpRefreshListener(refreshAction: (() -> Unit)? = null) {
        onUpRefreshAction = refreshAction
    }

    private var onDownRefreshAction: (() -> Unit)? = null
    fun setOnDownRefreshListener(refreshAction: (() -> Unit)? = null) {
        onDownRefreshAction = refreshAction
    }

    private var mTargetView: View? = null
        get() = field ?: childrenSequence().find { it != mLoadingView }?.also { field = it }

    var isRefreshing: Boolean
        get() = mRefreshing
        set(refreshing) = if (refreshing && mRefreshing != refreshing) {
            mRefreshing = refreshing
            mNotify = false
        } else {
            setRefreshing(refreshing, false)
        }

    init {
        setWillNotDraw(false)
        mDecelerateInterpolator = DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR)

        createProgressView()
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

    private fun createProgressView() {
        addView(mLoadingView)
        (mLoadingView.background as LAnimationDrawable).start()
    }

    private fun setRefreshing(refreshing: Boolean, notify: Boolean) {
        if (mRefreshing != refreshing) {
            mNotify = notify
            mRefreshing = refreshing
            if (mRefreshing) {
                val callback = if ((mTargetView?.translationY) ?: 0f > 0f) ::onDownRefreshAction else ::onUpRefreshAction
                callback.get()?.let { action ->
                    animateOffsetToCorrectPosition()
                    action()
                } ?: animateOffsetToStartPosition()
            } else {
                if (!mReturningToStart) {
                    animateOffsetToStartPosition()
                }
            }
        }
    }

    private val canChildScrollUp
        get() = (mTargetView as? ListView)?.let { ListViewCompat.canScrollList(it, -1) } ?: mTargetView?.canScrollVertically(-1) ?: false

    private val canChildScrollDown
        get() = (mTargetView as? ListView)?.let { ListViewCompat.canScrollList(it, 1) } ?: mTargetView?.canScrollVertically(1) ?: false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked

        if (!isEnabled || mReturningToStart || (canChildScrollDown && canChildScrollUp) || mRefreshing || mNestedScrollInProgress) return false

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
                if (!canChildScrollUp && canRefreshDown) {
                    startNegativeDragging(y)
                }
                if (!canChildScrollDown && canRefreshUp) {
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
        return (isEnabled && !mReturningToStart && !mRefreshing && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0)
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
        if (dy < 0 && !canChildScrollUp && canRefreshDown) {
            mTotalUnconsumed += Math.abs(dy).toFloat()
            processDragMotionDown(mTotalUnconsumed)
        } else if (dy > 0 && !canChildScrollDown && canRefreshUp) {
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
        val loadingViewY = (targetY - mLoadingView.measuredHeight) / 2f
        val loadingViewAlpha = (loadingViewY / mLoadingView.measuredHeight).coerceIn(0f, 1f)
        mTargetView?.translationY = targetY
        mLoadingView.translationY = loadingViewY
        mLoadingView.alpha = loadingViewAlpha
        mLoadingView.layoutParams = (mLoadingView.layoutParams as FrameLayout.LayoutParams).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL }
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
        val loadingViewY = (targetY/* - mLoadingView.measuredHeight*/) / 2f
        val loadingViewAlpha = (loadingViewY / mLoadingView.measuredHeight).coerceIn(0f, 1f)
        mTargetView?.translationY = -targetY
        mLoadingView.translationY = -loadingViewY
        mLoadingView.alpha = loadingViewAlpha
        mLoadingView.layoutParams = (mLoadingView.layoutParams as FrameLayout.LayoutParams).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL }
    }

    private fun finishSpinner(overscrollTop: Float) {
        if ((overscrollTop > mTotalDragDistance && mLoadingView.translationY > 0) || (-overscrollTop > mTotalDragDistance && mLoadingView.translationY < 0)) {
            setRefreshing(true, true)
        } else {
            mRefreshing = false
            animateOffsetToStartPosition()
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked

        if (!isEnabled || mReturningToStart || (canChildScrollDown && canChildScrollUp) || mRefreshing || mNestedScrollInProgress) return false

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mActivePointerId = ev.getPointerId(0)
                mIsBeingDragged = false
            }

            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) return false

                val y = ev.getY(pointerIndex)
                if (!canChildScrollUp && canRefreshDown) {
                    startNegativeDragging(y)
                }
                if (!canChildScrollDown && canRefreshUp) {
                    startPositiveDragging(y)
                }

                if (mIsBeingDragged) {
                    val overscrollTop = (y - mInitialMotionY) * DRAG_RATE
                    when {
                        overscrollTop > 0 && canRefreshDown -> {
                            mLoadingView.layoutParams = (mLoadingView.layoutParams as FrameLayout.LayoutParams).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL }
                            processDragMotionDown(overscrollTop)
                        }
                        overscrollTop < 0 && canRefreshUp -> {
                            mLoadingView.layoutParams = (mLoadingView.layoutParams as FrameLayout.LayoutParams).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL }
                            processDragMotionUp(overscrollTop)
                        }
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
            mLoadingView.layoutParams = (mLoadingView.layoutParams as FrameLayout.LayoutParams).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL }
        }
    }

    private fun startPositiveDragging(y: Float) {
        val yDiff = mInitialDownY - y
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop
            mIsBeingDragged = true
            mLoadingView.layoutParams = (mLoadingView.layoutParams as FrameLayout.LayoutParams).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL }
        }
    }

    private fun animateOffsetToCorrectPosition() {
        val targetY = dip(50).toFloat() * mLoadingView.translationY.sign
        val loadingViewY = (targetY - mLoadingView.measuredHeight) / 2f
        val duration = ((mTargetView?.translationY ?: 0f) / targetY % (targetY + 1) * 350).toLong().absoluteValue
        mReturningToStart = true
        mTargetView?.animate()?.translationY(targetY)?.setDuration(duration)?.setInterpolator(mDecelerateInterpolator)?.withEndAction { mReturningToStart = false; if (!mRefreshing) animateOffsetToStartPosition() }?.start()
        mLoadingView.animate()?.translationY(loadingViewY/* + (mLoadingView.measuredHeight.takeIf { mLoadingView.translationY < 0 } ?: 0)*/)?.alpha(1f)?.setDuration(duration)?.setInterpolator(mDecelerateInterpolator)?.start()
    }

    private fun animateOffsetToStartPosition() {
        val maxY = dip(50).toFloat()
        val duration = ((mTargetView?.translationY ?: 0f) / maxY % (maxY + 1) * 250).toLong().absoluteValue
        mReturningToStart = true
        mTargetView?.animate()?.translationY(0f)?.setDuration(duration)?.setInterpolator(mDecelerateInterpolator)?.withEndAction { mReturningToStart = false }?.start()
        mLoadingView.animate().translationY(-mLoadingView.measuredHeight * mLoadingView.translationY.sign).alpha(0f).setDuration(duration).setInterpolator(mDecelerateInterpolator).start()
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.actionIndex
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    enum class SwipeDirection {
        UP, DOWN
    }

    companion object {
        private val DECELERATE_INTERPOLATION_FACTOR = 2f
        private val INVALID_POINTER = -1
        private val DRAG_RATE = .5f
    }
}
