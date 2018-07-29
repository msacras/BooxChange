package nl.booxchange.screens.messages

import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.databinding.ObservableInt
import android.view.View
import com.vcristian.combus.post
import nl.booxchange.BooxchangeDatabase
import nl.booxchange.api.APIClient.Chat
import nl.booxchange.model.ChatModel
import nl.booxchange.model.ChatOpenedEvent
import nl.booxchange.utilities.BaseViewModel
import org.jetbrains.anko.doAsync


fun <T> LiveData<T>.observeForever(observer: (T) -> Unit) = observeForever { it?.let(observer) }

class MessagesFragmentViewModel: BaseViewModel() {
    val requestsList = LivePagedListBuilder(BooxchangeDatabase.instance.chatsDao().getRequests(), 100).build()
    val chatsList = LivePagedListBuilder(BooxchangeDatabase.instance.chatsDao().getChats(), 100).build()

    val chatsCount = ObservableInt(0)
    val unreadCount = ObservableInt(0)
    val requestCount = ObservableInt(0)

    init {
        doAsync {
            BooxchangeDatabase.instance.chatsDao().getUnreadMessagesCount().observeForever(unreadCount::set)
            BooxchangeDatabase.instance.chatsDao().getChatRequestsCount().observeForever(requestCount::set)
        }

        onRefresh()

/*
        expect(MessageReceivedEvent::class.java) { (messageModel) ->
            if (messageModel.userId == UserData.Session.userId) return@expect
            chatsList.get()?.find { it.id == messageModel.chatId }?.apply {
                unreadCount++
                lastMessage = messageModel
                updateUnreadMessagesCounter()
            } ?: onRefresh()
        }
*/

//        onRefresh()
    }

    private fun fetchChatsList() {
        Chat.fetchChatRooms {
            doAsync {
                BooxchangeDatabase.instance.chatsDao().insertChats(*it.orEmpty().toTypedArray())
            }
        }
    }

    private fun updateUnreadMessagesCounter() {
//        chatsList.get()?.map((ChatModel::unreadCount))?.sum()?.let(unreadCount::set)
    }

    fun onChatItemClick(view: View, chatModel: ChatModel) {
        post(ChatOpenedEvent(chatModel))
    }

    override fun onRefresh() {
        fetchChatsList()
    }
}
