package nl.booxchange.widget

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.support.annotation.ColorInt
import nl.booxchange.R
import nl.booxchange.extension.getColorById
import nl.booxchange.utilities.Tools.interpolateColor
import nl.booxchange.utilities.Tools.safeContext
import kotlin.math.absoluteValue


class LAnimationDrawable(
    frameRate: Int = 60,
    loopsPerSecond: Float = 1f,
    thickness: Float? = null,
    width: Float? = null,
    @ColorInt primaryColor: Int? = null,
    @ColorInt secondaryColor: Int? = null
) : AnimationDrawable() {
    private var _thicknessAnimator: ValueAnimator? = null
    var thickness = thickness
        set(value) {
            _thicknessAnimator?.cancel()
            _thicknessAnimator = ValueAnimator.ofFloat(frameAt(0).thickness!!, value!!).apply {
                addUpdateListener { anim ->
                    (0 until numberOfFrames).forEach { frameAt(it).thickness = anim.animatedValue as Float }
                }
                duration = ((field ?: 0-value).absoluteValue * 2).toLong()
                start()
            }
            field = value
        }

    private var _widthAnimator: ValueAnimator? = null
    var width = width
        set(value) {
            _widthAnimator?.cancel()
            val targetWidth = if (isEnabled) value!! else frameAt(0).length
            _widthAnimator = ValueAnimator.ofFloat(frameAt(0).width!!, targetWidth).apply {
                addUpdateListener { anim ->
                    (0 until numberOfFrames).forEach { frameAt(it).width = anim.animatedValue as Float }
                }
                duration = ((field ?: 0-targetWidth).absoluteValue * 2).coerceAtMost(200f).toLong()
                start()
            }
            field = value
        }

    private var _primaryColorAnimator: ValueAnimator? = null
    var primaryColor = primaryColor ?: Color.BLACK
        set(value) {
            _primaryColorAnimator?.cancel()
            _primaryColorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                val startColor = frameAt(0).primaryColor
                val endColor = if (isEnabled) value else safeContext.getColorById(R.color.midGray)
                addUpdateListener { anim ->
                    (0 until numberOfFrames).forEach { frameAt(it).primaryColor = interpolateColor(startColor, endColor, anim.animatedFraction) }
                }
                duration = ((FloatArray(3).apply { Color.colorToHSV(startColor, this) }[0] - FloatArray(3).apply { Color.colorToHSV(endColor, this) }[0]).absoluteValue * 10).toLong().coerceIn(100L..300L)
                start()
            }
        }

    private var _secondaryColorAnimator: ValueAnimator? = null
    var secondaryColor = secondaryColor ?: Color.WHITE
        set(value) {
            _secondaryColorAnimator?.cancel()
            _secondaryColorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                val startColor = frameAt(0).secondaryColor
                val endColor = if (isEnabled) value else safeContext.getColorById(R.color.midGray)
                addUpdateListener { anim ->
                    (0 until numberOfFrames).forEach { frameAt(it).secondaryColor = interpolateColor(startColor, endColor, anim.animatedFraction) }
                }
                duration = ((FloatArray(3).apply { Color.colorToHSV(startColor, this) }[0] - FloatArray(3).apply { Color.colorToHSV(endColor, this) }[0]).absoluteValue * 10).toLong().coerceIn(100L..300L)
                start()
            }
        }

    init {
        val duration = 1000 / frameRate
        val frames = (frameRate / loopsPerSecond).toInt()
        (0 until frames).forEach { frame ->
            addFrame(LDrawable(1f * frame / frames, this.thickness, this.width, this.primaryColor, this.secondaryColor), duration)
        }
        isOneShot = false
    }

    var isEnabled = true
        set(value) {
            field = value
            primaryColor = primaryColor
            secondaryColor = secondaryColor
            width = width
        }

    private fun frameAt(index: Int) = getFrame(index) as LDrawable
}
