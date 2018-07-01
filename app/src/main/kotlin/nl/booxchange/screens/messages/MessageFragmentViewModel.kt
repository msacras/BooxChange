package nl.booxchange.screens.messages

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.vcristian.combus.expect
import com.vcristian.combus.post
import nl.booxchange.api.APIClient.Chat
import nl.booxchange.model.*
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.UserData


class MessagesFragmentViewModel: BaseViewModel() {
    val requestsList = ObservableField<List<ChatModel>>()
    val chatsList = ObservableField<List<ChatModel>>()

    val chatsCount = ObservableInt(0)
    val unreadCount = ObservableInt(0)
    val requestCount = ObservableInt(0)

    init {
        expect(MessageReceivedEvent::class.java) { (messageModel) ->
            if (messageModel.userId == UserData.Session.userId) return@expect
            chatsList.get()?.find { it.id == messageModel.chatId }?.apply {
                unreadCount++
                lastMessage = messageModel
                updateUnreadMessagesCounter()
            } ?: onRefresh()
        }

        onRefresh()
    }

    private fun fetchChatsList() {
        Chat.fetchChatRooms("ROOMS") {
            it?.let(chatsList::set)
            chatsList.get()?.size?.let(chatsCount::set)
            updateUnreadMessagesCounter()
            onLoadingFinished()
        }
    }

    private fun fetchRequestsList() {
        Chat.fetchChatRooms("REQUESTS") {
            it?.let(requestsList::set)
            requestsList.get()?.size?.let(requestCount::set)
            onLoadingFinished()
        }
    }

    private fun updateUnreadMessagesCounter() {
        chatsList.get()?.map((ChatModel::unreadCount))?.sum()?.let(unreadCount::set)
    }

    fun onChatItemClick(view: View, chatModel: ChatModel) {
        post(ChatOpenedEvent(chatModel))
    }

    override fun onRefresh() {
        fetchChatsList()
        fetchRequestsList()
    }
}
