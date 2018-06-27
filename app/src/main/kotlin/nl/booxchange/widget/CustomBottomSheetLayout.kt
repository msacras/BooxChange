package nl.booxchange.widget

import android.content.Context
import android.graphics.Color
import android.support.v4.widget.ViewDragHelper.INVALID_POINTER
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.vcristian.combus.dismiss
import nl.booxchange.R
import org.jetbrains.anko.find
import org.jetbrains.anko.withAlpha
import kotlin.math.absoluteValue


class CustomBottomSheetLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null): FrameLayout(context, attrs) {

    private val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var initialTime = 0L

    private var mInitialMotionY = 0f
    private var mInitialDownY = 0f
    private var mIsBeingDragged = false
    private var mActivePointerId = INVALID_POINTER

    private var expandedPosition = 0f
    private var startPosition = 0f
    private var maxPosition = 0f

    private var mReturningToStart = false
    private val mDecelerateInterpolator = DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR)

    private var onDismissAction: (() -> Unit)? = null
    fun setOnDismissAction(dismissAction: (() -> Unit)? = null) {
        onDismissAction = dismissAction
    }

    private lateinit var targetView: View

    init {
        setOnClickListener { dismiss() }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked

        if (!isEnabled) return false

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
                startDragging(y)
            }

            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
            }
        }

        return mIsBeingDragged
    }

    private fun processDragMotion(motionDelta: Float) {
        targetView.translationY = (targetView.translationY - motionDelta).coerceAtLeast(maxPosition)
        if (targetView.translationY > startPosition) {
            setBackgroundColor(Color.BLACK.withAlpha(((height - targetView.translationY) / startPosition * 0x80).toInt().coerceIn(0..0x80)))
        }
    }

    private fun finishSpinner() {
        when {
            targetView.translationY < 0f -> return
            targetView.translationY > startPosition + targetView.find<View>(R.id.info_block_start).bottom * 0.3 -> animateOffsetToClosedPosition()
            targetView.translationY < startPosition - targetView.find<View>(R.id.info_block_start).bottom * 0.5 -> animateOffsetToExpandedPosition()
            else -> animateOffsetToStartPosition()
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked

        if (!isEnabled) return false

        when (action) {
            MotionEvent.ACTION_DOWN -> {
//                if (ev.y < targetView.translationY) mInitialDownY = ev.y
                initialTime = System.currentTimeMillis()
                mActivePointerId = ev.getPointerId(0)
                mIsBeingDragged = false
            }

            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) return false
//                if (ev.y < targetView.translationY) return false

                val y = ev.getY(pointerIndex)
                startDragging(y)

                if (mIsBeingDragged) {
                    val delta = mInitialMotionY - y
                    processDragMotion(delta)
                    mInitialMotionY = y
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
                    mIsBeingDragged = false
                    if (System.currentTimeMillis() - initialTime < 200) {

                    } else {
                        finishSpinner()
                    }
                } else {
                    if (ev.y < targetView.translationY && ev.y == mInitialDownY) {
                        dismiss()
                    }
                }
                mActivePointerId = INVALID_POINTER
                return false
            }

            MotionEvent.ACTION_CANCEL -> return false
        }

        return true
    }

    private fun startDragging(y: Float) {
        val yDiff = y - mInitialDownY
        if (yDiff.absoluteValue > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop
            mIsBeingDragged = true
        }
    }

    private fun animateOffsetToClosedPosition() {
        val duration = ((targetView.translationY / targetView.height).absoluteValue * 400).toLong()
        mReturningToStart = true
        targetView.animate().translationY(targetView.height.toFloat()).setDuration(1000L).setInterpolator {
            setBackgroundColor(Color.BLACK.withAlpha(((height - targetView.translationY) / startPosition * 0x80).toInt().coerceIn(0..0x80)))
            mDecelerateInterpolator.getInterpolation(it)
        }.withEndAction { mReturningToStart = false; onDismissAction?.invoke() }.start()
    }

    private fun animateOffsetToExpandedPosition() {
        val duration = ((targetView.translationY / targetView.height).absoluteValue * 400).toLong()
        mReturningToStart = true
        targetView.animate().translationY(expandedPosition).setDuration(400L).setInterpolator {
            setBackgroundColor(Color.BLACK.withAlpha(((height - targetView.translationY) / startPosition * 0x80).toInt().coerceIn(0..0x80)))
            mDecelerateInterpolator.getInterpolation(it)
        }.withEndAction { mReturningToStart = false }.start()
    }

    private fun animateOffsetToStartPosition() {
        val duration = ((targetView.translationY / targetView.height).absoluteValue * 400).toLong()
        mReturningToStart = true
        targetView.animate().translationY(startPosition).setDuration(400L).setInterpolator {
            setBackgroundColor(Color.BLACK.withAlpha(((height - targetView.translationY) / startPosition * 0x80).toInt().coerceIn(0..0x80)))
            mDecelerateInterpolator.getInterpolation(it)
        }.withEndAction { mReturningToStart = false }.start()
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.actionIndex
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    fun appear() {
        targetView.measure(View.MeasureSpec.makeMeasureSpec(targetView.width, View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED)
        targetView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, targetView.measuredHeight)
        startPosition = height - targetView.find<View>(R.id.info_block_start).bottom - 0f
        maxPosition = height - targetView.measuredHeight - 0f
        expandedPosition = maxPosition.coerceAtLeast(0f)
        targetView.translationY = targetView.measuredHeight.toFloat()
        animateOffsetToStartPosition()
    }

    fun dismiss() {
        animateOffsetToClosedPosition()
    }

    override fun addView(child: View, params: ViewGroup.LayoutParams?) {
        targetView = child
        super.addView(child, params)
    }

    companion object {
        private const val DECELERATE_INTERPOLATION_FACTOR = 2f
        private const val INVALID_POINTER = -1
        private const val DRAG_RATE = .5f
    }
}
