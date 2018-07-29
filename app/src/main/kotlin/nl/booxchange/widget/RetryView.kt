package nl.booxchange.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.retry_view_layout.view.*
import nl.booxchange.R
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
import kotlin.properties.Delegates


/**
 * Created by Cristian Velinciuc on 3/24/18.
 */
class RetryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): FrameLayout(context, attrs, defStyleAttr) {

    var message: String by Delegates.observable("") { _, _, _ -> info_message?.text = message }
    var action: () -> Unit = {}

    init {
        View.inflate(context, R.layout.retry_view_layout, this)
        setBackgroundColor(Color.WHITE)
        alpha = 0f
        id = R.id.retry_view
        toGone()
    }

    fun show(smooth: Boolean = true) {
        toVisible()
        if (smooth) {
            animate().alpha(1f).setDuration(100).start()
        }
    }

    fun hide(smooth: Boolean = true, delayed: Boolean = true) {
        val delay = if (delayed) 500L else 1L
        val animation = if (smooth) 200L else 1L
        postDelayed({ animate().alpha(0f).setDuration(animation).withEndAction { toGone() }.start() }, delay)
    }
}
