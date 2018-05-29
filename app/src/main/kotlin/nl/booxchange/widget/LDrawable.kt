package nl.booxchange.widget

import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import nl.booxchange.BooxchangeApp
import org.jetbrains.anko.dip


class LDrawable(
    var completion: Float = 0f,
    var thickness: Float? = null,
    var width: Float? = null,
    @ColorInt var primaryColor: Int = Color.BLACK,
    @ColorInt var secondaryColor: Int = Color.WHITE
): Drawable() {
    override fun draw(canvas: Canvas) {
        val radius = Math.min(bounds.width(), bounds.height()).toFloat()
        val paint = Paint()
        val thickness = thickness ?: (Math.max(bounds.width(), bounds.height()).toFloat() / 30).also { thickness = it }
        val padding = thickness / 2
        val width = width ?: (thickness * 4).also { width = it }
        val basePath = Path()
        basePath.addRoundRect(bounds.let { RectF(1f * it.left + padding, 1f * it.top + padding, 1f * it.right - padding, 1f * it.bottom - padding) }, dip2px(radius), dip2px(radius), Path.Direction.CW)
        val dashPath = Path()
        PathMeasure(basePath, false).let { pathMeasure ->
            val startPoint = pathMeasure.length * completion
            val endPoint = pathMeasure.length * completion + width
            pathMeasure.getSegment(startPoint, endPoint, dashPath, true)
            if (endPoint > pathMeasure.length) {
                val overflowPoint = endPoint - pathMeasure.length
                pathMeasure.getSegment(0f, overflowPoint, dashPath, true)
            }
        }

        paint.style = Paint.Style.STROKE
        paint.color = primaryColor
        paint.strokeWidth = thickness
        paint.isAntiAlias = true
        canvas.drawPath(basePath, paint)
        paint.color = secondaryColor
        canvas.drawPath(dashPath, paint)
    }

    var length: Float = 0f
        private set
        get() {
            val radius = Math.min(bounds.width(), bounds.height()).toFloat()
            val thickness = thickness ?: (Math.max(bounds.width(), bounds.height()).toFloat() / 30).also { thickness = it }
            val padding = thickness / 2
            val basePath = Path().apply { addRoundRect(bounds.let { RectF(1f * it.left + padding, 1f * it.top + padding, 1f * it.right - padding, 1f * it.bottom - padding) }, dip2px(radius), dip2px(radius), Path.Direction.CW) }
            return PathMeasure(basePath, false).length
        }

    override fun setAlpha(alpha: Int) {}
    override fun getOpacity() = PixelFormat.TRANSLUCENT
    override fun setColorFilter(colorFilter: ColorFilter?) {}

    private fun dip2px(dips: Float) = BooxchangeApp.delegate.applicationContext.dip(dips).toFloat()
}
