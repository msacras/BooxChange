package nl.booxchange.screens.messages

import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.vcristian.combus.expect
import com.vcristian.combus.post
import nl.booxchange.BooxchangeDatabase
import nl.booxchange.api.APIClient.Chat
import nl.booxchange.model.*
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.UserData
import org.jetbrains.anko.doAsync


class MessagesFragmentViewModel: BaseViewModel() {
    val requestsList = LivePagedListBuilder(BooxchangeDatabase.instance.chatsDao().getRequests(), 20).setBoundaryCallback(object: PagedList.BoundaryCallback<ChatModel>() {
        override fun onZeroItemsLoaded() {
            super.onZeroItemsLoaded()
        }

        override fun onItemAtEndLoaded(itemAtEnd: ChatModel) {
            super.onItemAtEndLoaded(itemAtEnd)
        }

        override fun onItemAtFrontLoaded(itemAtFront: ChatModel) {
            super.onItemAtFrontLoaded(itemAtFront)
        }
    }).build()

    val chatsList = LivePagedListBuilder(BooxchangeDatabase.instance.chatsDao().getChats(), 20).setBoundaryCallback(object: PagedList.BoundaryCallback<ChatModel>() {
        override fun onZeroItemsLoaded() {
            super.onZeroItemsLoaded()
        }

        override fun onItemAtEndLoaded(itemAtEnd: ChatModel) {
            super.onItemAtEndLoaded(itemAtEnd)
        }

        override fun onItemAtFrontLoaded(itemAtFront: ChatModel) {
            super.onItemAtFrontLoaded(itemAtFront)
        }
    }).build()

    val chatsCount = ObservableInt(0)
    val unreadCount = ObservableInt(0)
    val requestCount = ObservableInt(0)

    init {
        doAsync {

        }

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
/*
        Chat.fetchChatRooms("ROOMS") {
            it?.let(chatsList::set)
            chatsList.get()?.size?.let(chatsCount::set)
            updateUnreadMessagesCounter()
            onLoadingFinished()
        }
*/
    }

    private fun fetchRequestsList() {
/*
        Chat.fetchChatRooms("REQUESTS") {
            it?.let(requestsList::set)
            requestsList.get()?.size?.let(requestCount::set)
            onLoadingFinished()
        }
*/
    }

    private fun updateUnreadMessagesCounter() {
//        chatsList.get()?.map((ChatModel::unreadCount))?.sum()?.let(unreadCount::set)
    }

    fun onChatItemClick(view: View, chatModel: ChatModel) {
        post(ChatOpenedEvent(chatModel))
    }

    override fun onRefresh() {
        fetchChatsList()
        fetchRequestsList()
    }
}
