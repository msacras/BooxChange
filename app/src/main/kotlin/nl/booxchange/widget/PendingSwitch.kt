package nl.booxchange.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Checkable
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.pending_switch_layout.view.*
import nl.booxchange.R
import nl.booxchange.utilities.Tools.interpolateColor
import nl.booxchange.utilities.Tools.interpolateValue
import org.jetbrains.anko.dip


class PendingSwitch @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attributeSet, defStyleAttr), Checkable {

    private val MAX_CLICK_DURATION = 200
    private val THUMB_SIZE = dip(15f)

    val isPending
        get() = isChecked == null

    fun setPending() {
        isChecked = null
    }

    private var isChecked: Boolean? = null
        set(value) {
            if (field != value && value != null) {
                onCheckedChangeListener?.invoke(value)
            }
            field = value
            renderThumbState()
        }

    override fun isChecked() = isChecked == true
    override fun toggle() {
        isChecked = isChecked == false
    }

    override fun setChecked(checked: Boolean) {
        isChecked = checked
    }

    private val colors = mapOf(
        "true" to (0xFFFA7F5AL.toInt() to 0xFFFFDFD5L.toInt()),
        "false" to (0xFFECECECL.toInt() to 0xFFBDBDBDL.toInt()),
        "null" to (0xFF00ADEBL.toInt() to 0xFFBDBDBDL.toInt()),
        "disabled" to (0xFFBDBDBDL.toInt() to 0xFFECECECL.toInt())
    )

    private var onBeforeCheckedListener: ((willBeChecked: Boolean) -> Boolean)? = null
    private var onCheckedChangeListener: ((isChecked: Boolean) -> Unit)? = null

    init {
        View.inflate(context, R.layout.pending_switch_layout, this)
        initializeThumbGestureHandler()
        isChecked = false
    }

    private fun initializeThumbGestureHandler() {
        val touchStartPoint = PointF()
        var touchMotionLastX = 0f
        var touchStartTime = 0L

        switch_thumb.setOnTouchListener { _, event ->
            if (!isEnabled || isChecked == null) return@setOnTouchListener true

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchStartPoint.x = event.rawX
                    touchStartPoint.y = event.rawY
                    touchMotionLastX = event.rawX
                    touchStartTime = System.currentTimeMillis()
                }
                MotionEvent.ACTION_MOVE -> {
                    val touchMotionDifferenceX = event.rawX - touchMotionLastX
                    switch_thumb.translationX = (switch_thumb.translationX + touchMotionDifferenceX).coerceIn(0f, THUMB_SIZE.toFloat())
                    touchMotionLastX = event.rawX
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val touchEndTime = System.currentTimeMillis()
                    val gestureDuration = touchEndTime - touchStartTime

                    val willBeChecked = if (gestureDuration < MAX_CLICK_DURATION && touchStartPoint.equals(event.rawX, event.rawY)) {
                        isChecked == false
                    } else {
                        if (isChecked == true) {
                            switch_thumb.translationX > THUMB_SIZE * 1.2
                        } else {
                            switch_thumb.translationX > THUMB_SIZE * 0.6
                        }
                    }
                    if (willBeChecked != isChecked) {
                        onInternallyChecked(willBeChecked)
                    } else {
                        renderThumbState()
                    }
                }
            }
            true
        }

        switch_track.setOnClickListener { isChecked?.let { onInternallyChecked(isChecked == false) } }
    }

    private fun onInternallyChecked(willBeChecked: Boolean) {
        onBeforeCheckedListener?.invoke(willBeChecked)?.let { willCheck -> if (willCheck) null else isChecked = null } ?: run { isChecked = willBeChecked }
    }

    fun setOnBeforeCheckedListener(onBeforeCheckedAction: ((willBeChecked: Boolean) -> Boolean)?) {
        onBeforeCheckedListener = onBeforeCheckedAction
    }

    fun setOnCheckedChangeListener(onCheckedAction: ((isChecked: Boolean) -> Unit)?) {
        onCheckedChangeListener = onCheckedAction
    }

    private var thumbAnimator: ValueAnimator? = null
    private fun renderThumbState() {
        val (newThumbColor, newTrackColor) = if (isEnabled) colors[isChecked.toString()]!! else colors["disabled"]!!
        val (oldThumbColor, oldTrackColor) = (switch_thumb.tag as? Int ?: newThumbColor) to (switch_track.tag as? Int ?: newTrackColor)

        thumbAnimator?.cancel()
        thumbAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            val (thumbInitialWidth, thumbInitialTranslation) = switch_thumb.layoutParams.width to switch_thumb.translationX
            val (thumbTargetWidth, thumbTargetTranslation, translationDistance) = when (isChecked) {
                true -> Triple(THUMB_SIZE, THUMB_SIZE, THUMB_SIZE - switch_thumb.translationX.toLong())
                false -> Triple(THUMB_SIZE, 0, switch_thumb.translationX.toLong())
                else -> Triple(THUMB_SIZE * 2, 0, switch_thumb.translationX.toLong())
            }
            //duration = if (translationDistance == 0L) 150 else (translationDistance / switch_track.measuredWidth) * 350
            //interpolator = DecelerateInterpolator(2f)
            addUpdateListener {
                val percentComplete = it.animatedFraction
                switch_thumb.layoutParams.width = interpolateValue(thumbInitialWidth.toFloat(), thumbTargetWidth.toFloat(), percentComplete).toInt()
                switch_thumb.translationX = interpolateValue(thumbInitialTranslation, thumbTargetTranslation.toFloat(), percentComplete)
                switch_thumb.requestLayout()
                interpolateColor(oldThumbColor, newThumbColor, percentComplete).let {
                    switch_thumb.background.setColorFilter(it, PorterDuff.Mode.SRC_ATOP)
                    switch_thumb.tag = it
                }
                interpolateColor(oldTrackColor, newTrackColor, percentComplete).let {
                    switch_track.background.setColorFilter(it, PorterDuff.Mode.SRC_ATOP)
                    switch_track.tag = it
                }
            }
        }
        thumbAnimator?.start()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        renderThumbState()
    }
}
