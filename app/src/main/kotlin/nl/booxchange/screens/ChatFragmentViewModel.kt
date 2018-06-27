package nl.booxchange.screens

import android.databinding.*
import com.vcristian.combus.expect
import nl.booxchange.api.APIClient.Chat
import nl.booxchange.extension.takeNotBlank
import nl.booxchange.model.*
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.UserData
import org.joda.time.DateTime


class ChatFragmentViewModel: BaseViewModel() {
    val isSending = ObservableBoolean()

    private val chatMessagesMap = mutableMapOf<ChatModel, MutableSet<MessageModel>>()
    val messagesList = ObservableMutableSet<MessageModel>()

    var chatModel: ChatModel? = null

    val messageInput = ObservableField<String>()

    init {
        expect(MessageReceivedEvent::class.java) { (messageModel) ->
//            val targetChat = chatMessagesMap.keys.find { it.id == messageModel.chatId }
//            targetChat?.unreadCount?.inc()//?.set(targetChat.unreadCount.get() + 1)
//            chatMessagesMap[targetChat]?.add(messageModel)
            if (messageModel.chatId == chatModel?.id) {
                messagesList.add(messageModel)
            }
        }

        expect(ChatOpenedEvent::class.java) { event ->
            event.chatModel?.let(::bindChatModel) ?: fetchChat(event.chatId)
        }
    }

    private fun bindChatModel(chatModel: ChatModel) {
        messagesList.set(chatMessagesMap[chatModel] ?: mutableSetOf())
        chatMessagesMap[chatModel] = messagesList.get()
        this.chatModel = chatModel
        fetchLatestMessages()
    }

    private fun fetchChat(chatId: String) {
        Chat.fetchChatRoom(chatId) {
            it?.let(::bindChatModel)
        }
    }

    fun fetchLatestMessages() {
        val messageId = messagesList.get().lastOrNull()?.id
        Chat.fetchMessagesAfterId(chatModel?.id ?: return, messageId) {
            it?.let(messagesList::addAll) ?: onLoadingFailed()
            onLoadingFinished()
        }
    }

    fun fetchOlderMessages() {
        val messageId = messagesList.get().firstOrNull()?.id
        Chat.fetchMessagesBeforeId(chatModel?.id ?: return, messageId) {
            it?.let(messagesList::addAll) ?: onLoadingFailed()
            onLoadingFinished()
        }
    }

    fun sendMessage() {
        messageInput.get()?.takeNotBlank ?: return
        val messageModel = MessageModel("", chatModel?.id!!, UserData.Session.userId, messageInput.get()!!, MessageType.TEXT, DateTime.now())
        isSending.set(true)
        Chat.postMessage(messageModel) {
            it?.let {
                messagesList.add(it)
                messageInput.set("")
            }
            isSending.set(false)
        }
    }

    fun sendMessageReceivedStatus() {
        if (chatModel?.unreadCount/*?.get()*/ != 0) {
            chatModel?.unreadCount = 0//?.set(0)
            Chat.postMessageReceived(chatModel?.id ?: return)
        }
    }

    fun getAuthorByUserId(senderId: String): String? {
        return chatModel?.usersList?.find { it.id == senderId }?.getFormattedName()
    }

    fun getImageByUserId(senderId: String): List<String?> {
        return listOf(chatModel?.usersList?.find { it.id == senderId }?.photo)
    }

    fun isUserOwnedMessage(senderId: String): Boolean {
        return senderId == UserData.Session.userId
    }

    override fun onRefresh() {
        fetchLatestMessages()
    }
}

class ObservableMutableSet<T>: ObservableField<MutableSet<T>>()/*, MutableSet<T>*/ {
    init {
        set(mutableSetOf())
    }

    fun add(element: T): Boolean = get().add(element).also { notifyChange() }

    fun addAll(elements: Collection<T>): Boolean = get().addAll(elements).also { notifyChange() }

    fun clear() = get().clear().also { notifyChange() }

    fun iterator(): MutableIterator<T> = get().iterator()

    fun remove(element: T): Boolean = get().remove(element).also { notifyChange() }

    fun removeAll(elements: Collection<T>): Boolean = get().removeAll(elements).also { notifyChange() }

    fun retainAll(elements: Collection<T>): Boolean = get().retainAll(elements).also { notifyChange() }

    val size = get().size

    fun contains(element: T): Boolean = get().contains(element)

    fun containsAll(elements: Collection<T>): Boolean = get().containsAll(elements)

    fun isEmpty(): Boolean = get().isEmpty()

    override fun set(value: MutableSet<T>) = super.set(value)

    override fun get(): MutableSet<T> = super.get()!!
}
