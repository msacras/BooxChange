package nl.booxchange.screens.chat

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.vcristian.combus.expect
import kotlinx.android.synthetic.main.fragment_chat.view.*
import nl.booxchange.R
import nl.booxchange.extension.isVisible
import nl.booxchange.extension.setTintCompat
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
import nl.booxchange.model.ChatOpenedEvent
import nl.booxchange.screens.MainFragmentActivity
import nl.booxchange.utilities.BaseFragment
import org.jetbrains.anko.dip

class ChatFragment: BaseFragment() {

    override val contentViewResourceId = R.layout.fragment_chat
    override val viewModel = ChatFragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.app_bar_layout.toolbar.setNavigationOnClickListener { onBackPressed() }
        view.app_bar_layout.toolbar.navigationIcon?.setTintCompat(R.color.darkGray)

        expect(ChatOpenedEvent::class.java) {
            (activity as? MainFragmentActivity)?.showFragment("chat_view", false)
        }

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
                viewModel.removeSendableImage()
            }
        }

        view.messages_list.addItemDecoration(object: RecyclerView.ItemDecoration() {
            val `4dp` = view.dip(4)
            val `8dp` = view.dip(8)

            override fun getItemOffsets(outRect: Rect, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                outRect.set(`8dp`, `4dp`, `8dp`, `4dp`)
            }
        })

        view.messages_list.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!recyclerView.canScrollVertically(1)) {
                        viewModel.sendMessageReceivedStatus()
                    }
                }
            }
        })

        view.swipe_refresh_layout.canRefreshUp = true
        view.swipe_refresh_layout.setOnUpRefreshListener(viewModel::fetchLatestMessages)
        view.swipe_refresh_layout.setOnDownRefreshListener(viewModel::fetchOlderMessages)
    }

    override fun onBackPressed(): Boolean {
//        val view = view ?: return true

//        if (view.image_pager.isVisible) {
//            view.image_pager.toGone()
//            view.app_bar_layout.animate().alpha(1f).start()
//        } else {
        viewModel.chatModel = null
        (activity as? MainFragmentActivity)?.hideFragment("chat_view")
//        }

        return isHidden
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
