/*
package nl.booxchange.screens

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.list_item_chat.view.*
import nl.booxchange.R
import nl.booxchange.api.APIClient.Chat
import nl.booxchange.model.ChatModel
import nl.booxchange.utilities.BaseActivity
import nl.booxchange.utilities.Constants
import nl.booxchange.utilities.MessageUtilities
import nl.booxchange.utilities.RecyclerViewAdapter
import org.jetbrains.anko.startActivity

*/
/**
 * Created by Dima on 3/10/2018.
 *//*

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
    if (!swipe_refresh_layout.isRefreshing) //loadingView.show()
    Chat.fetchChatRooms {
      it?.let(chatRoomsAdapter::swapItems) //?: retryView.show()
      if (swipe_refresh_layout.isRefreshing) swipe_refresh_layout.isRefreshing = false
    }
  }

  private fun initializeSwipeRefreshLayout() {
    swipe_refresh_layout.setOnDownRefreshListener {
      fetchChatRooms()
    }
  }
}
*/
