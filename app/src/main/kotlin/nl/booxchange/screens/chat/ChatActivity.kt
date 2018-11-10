package nl.booxchange.screens.chat

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_chat.*
import nl.booxchange.R
import nl.booxchange.BR
import nl.booxchange.databinding.*
import nl.booxchange.extension.*
import nl.booxchange.utilities.BaseActivity
import nl.booxchange.utilities.Constants
import org.jetbrains.anko.contentView
import org.jetbrains.anko.dip
import org.jetbrains.anko.findOptional

class ChatActivity: BaseActivity() {
    override val viewModel = ChatActivityViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = DataBindingUtil.setContentView<ViewDataBinding>(this, R.layout.activity_chat)

        viewModel.initializeWithConfig(intent.getStringExtra(KEY_CHAT_ID))
        viewBinding.setVariable(BR.viewModel, viewModel)

        setupView()
    }

    private fun setupView() {
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener { onBackPressed() }
        toolbar?.navigationIcon?.setTintCompat(R.color.darkGray)

        send_message.isEnabled = false

        message_input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                send_message.isEnabled = !s.toString().trim().isEmpty()
            }
        })

        val `40dp` = dip(40).toFloat()

        send_more.setOnClickListener {
            if (message_input.isEnabled) {
                if (buttons_container.isVisible) {
                    buttons_container.animate().alpha(0f).translationY(`40dp`).withEndAction(buttons_container::toGone).setDuration(150).start()
                    send_more.animate().rotation(0f).setDuration(150).start()
                } else {
                    buttons_container.animate().alpha(1f).translationY(0f).withStartAction(buttons_container::toVisible).setDuration(150).start()
                    send_more.animate().rotation(135f).setDuration(150).start()
                }
            } else {
                send_more.animate().rotation(0f).setDuration(150).start()
                viewModel.imageInput.set(null)
            }
        }

        messages_list.addItemDecoration(object: RecyclerView.ItemDecoration() {
            val vertical = dip(4)

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(0, vertical, 0, vertical)
            }
        })

        messages_list.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (recyclerView.computeVerticalScrollOffset() == 0) {
                        viewModel.fetchPreviousMessages()
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
/*
        val view = view ?: return true

        if (image_pager.isVisible) {
            image_pager.toGone()
            app_bar_layout.animate().alpha(1f).start()
        } else {
        viewModel.chatModel = null
*/
//        (activity as? MainFragmentActivity)?.hideFragment("chat_view")
/*
        }
*/
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode in listOf(Constants.REQUEST_CAMERA, Constants.REQUEST_GALLERY)) {
            buttons_container?.animate()?.alpha(0f)?.translationY(dip(40).toFloat())?.withEndAction(buttons_container::toGone)?.setDuration(150)?.start()
            send_more?.animate()?.rotation(135f)?.setDuration(150)?.start()
            viewModel.onActivityResult(requestCode, resultCode, data)

            send_message.isEnabled = true
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val KEY_CHAT_ID = "CHAT_ID"
    }
}
