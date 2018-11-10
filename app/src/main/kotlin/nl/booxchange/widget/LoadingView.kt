package nl.booxchange.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import kotlinx.android.synthetic.main.loading_view_layout.view.*
import nl.booxchange.R
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
import kotlin.properties.Delegates


class LoadingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    var message: String by Delegates.observable("") { _, _, _ -> info_message?.text = message }

    init {
        View.inflate(context, R.layout.loading_view_layout, this)
        background = ColorDrawable(Color.WHITE)
        logo_image.background = AnimatedVectorDrawableCompat.create(context, R.drawable.booxchange_loading)
        id = R.id.loading_view
        toGone()
    }

    fun show(smooth: Boolean = true) {
        toVisible()
        (logo_image.background as? AnimatedVectorDrawableCompat)?.start()
        if (smooth) {
            animate().alpha(1f).setDuration(100).start()
        }
    }

    fun hide(smooth: Boolean = true, delayed: Boolean = true) {
        val delay = if (delayed) 500L else 1L
        val animation = if (smooth) 200L else 1L
        postDelayed({ animate().alpha(0f).setDuration(animation).withEndAction { (logo_image.background as? AnimatedVectorDrawableCompat)?.stop(); toGone() }.start() }, delay)
    }
}
