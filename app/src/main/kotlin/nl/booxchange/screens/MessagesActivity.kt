package nl.booxchange.screens

import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.list_item_chat.view.*
import nl.booxchange.R
import nl.booxchange.R.id.chats_list
import nl.booxchange.R.id.swipe_refresh_layout
import nl.booxchange.model.ChatModel
import nl.booxchange.utilities.*
import org.jetbrains.anko.startActivity

/**
 * Created by Dima on 3/10/2018.
 */
class MessagesActivity: BaseActivity() {
  private val chatRoomsAdapter = RecyclerViewAdapter()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_messages)
    chats_list.layoutManager = LinearLayoutManager(this)
    chats_list.adapter = chatRoomsAdapter.apply {
      addModelToViewBinding(R.layout.list_item_chat, ChatModel::class) { view, model ->
        view.chat_topic.text = model.topic
        view.last_message.text = MessageUtilities.getActionCommentary(model.lastMessage, model.usersList)
        view.setOnClickListener { startActivity<ChatActivity>(Constants.EXTRA_PARAM_CHAT_MODEL to model) }
      }
    }

    fetchChatRooms()
    initializeSwipeRefreshLayout()
  }

  private fun fetchChatRooms() {
    if (!swipe_refresh_layout.isRefreshing) loadingView.show()
    requestManager.fetchChatRooms {
      it?.let(chatRoomsAdapter::swapItems) ?: retryView.show()
      if (!swipe_refresh_layout.isRefreshing) loadingView.hide() else swipe_refresh_layout.isRefreshing = false
    }
  }

  private fun initializeSwipeRefreshLayout() {
    swipe_refresh_layout.setOnDownRefreshListener {
      fetchChatRooms()
    }
  }
}
