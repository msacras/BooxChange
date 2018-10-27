package nl.booxchange.screens.chat

import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.vcristian.combus.expect
import kotlinx.android.synthetic.main.fragment_chat.view.*
import nl.booxchange.R
import nl.booxchange.databinding.ChatItemImageBinding
import nl.booxchange.databinding.ChatItemMessageBinding
import nl.booxchange.databinding.ChatItemRequestBinding
import nl.booxchange.extension.isVisible
import nl.booxchange.extension.setTintCompat
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
import nl.booxchange.model.events.ChatOpenedEvent
import nl.booxchange.screens.MainFragmentActivity
import nl.booxchange.utilities.BaseFragment
import nl.booxchange.utilities.Constants
import org.jetbrains.anko.dip
import org.jetbrains.anko.findOptional

class ChatFragment: BaseFragment() {

    override val contentViewResourceId = R.layout.fragment_chat
    override val viewModel = ChatFragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        view.app_bar_layout.toolbar.setNavigationOnClickListener { onBackPressed() }
//        view.app_bar_layout.toolbar.navigationIcon?.setTintCompat(R.color.darkGray)

        val `40dp` = view.dip(40).toFloat()

        view.send_more.setOnClickListener {
            if (view.message_input.isEnabled) {
                if (view.buttons_container.isVisible) {
                    view.buttons_container.animate().alpha(0f).translationY(`40dp`).withEndAction(view.buttons_container::toGone).setDuration(150).start()
                    view.send_more.animate().rotation(0f).setDuration(150).start()
                } else {
                    view.buttons_container.animate().alpha(1f).translationY(0f).withStartAction(view.buttons_container::toVisible).setDuration(150).start()
                    view.send_more.animate().rotation(135f).setDuration(150).start()
                }
            } else {
                view.send_more.animate().rotation(0f).setDuration(150).start()
                viewModel.imageInput.set(null)
            }
        }

        view.messages_list.addItemDecoration(object: RecyclerView.ItemDecoration() {
            val vertical = view.dip(2)
            val horizontal = view.dip(8)
            val ownership = view.dip(72)

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val binding = DataBindingUtil.getBinding<ViewDataBinding>(view)
                val model = (binding as? ChatItemMessageBinding)?.itemModel
                    ?: (binding as? ChatItemRequestBinding)?.itemModel
                    ?: (binding as? ChatItemImageBinding)?.itemModel

                var left = horizontal
                var right = horizontal

                if (model?.type != "REQUEST") {
                    val contentView = view.findOptional<View>(R.id.chat_item_content) ?: return

                    contentView.layoutParams = (contentView.layoutParams as FrameLayout.LayoutParams).apply {
                        if (model?.isOwnMessage == true) {
                            left += ownership
                            gravity = Gravity.END
                        } else {
                            right += ownership
                            gravity = Gravity.START
                        }
                    }
                }

                outRect.set(left, vertical, right, vertical)
            }
        })

        view.messages_list.setOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (recyclerView.computeVerticalScrollOffset() == 0) {
                        viewModel.fetchPreviousMessages()
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode in listOf(Constants.REQUEST_CAMERA, Constants.REQUEST_GALLERY)) {
            view?.buttons_container?.animate()?.alpha(0f)?.translationY(view!!.context.dip(40).toFloat())?.withEndAction(view!!.buttons_container::toGone)?.setDuration(150)?.start()
            view?.send_more?.animate()?.rotation(135f)?.setDuration(150)?.start()
            viewModel.onActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
