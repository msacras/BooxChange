package nl.booxchange.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.loading_view_layout.view.*
import nl.booxchange.R
import nl.booxchange.extension.getColorById
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
import org.jetbrains.anko.withAlpha
import kotlin.properties.Delegates


/**
 * Created by Cristian Velinciuc on 3/24/18.
 */
class RetryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): FrameLayout(context, attrs, defStyleAttr) {

  var message: String by Delegates.observable("") { _, _, _ -> info_message?.text = message }
  var action: () -> Unit = {}

  init {
    View.inflate(context, R.layout.retry_view_layout, this)
    setBackgroundColor(Color.WHITE.withAlpha(200))
  }

  fun show() {
    toVisible()
  }

  fun hide() {
    toGone()
  }
}
