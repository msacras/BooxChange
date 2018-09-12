package nl.booxchange.screens.messages

import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableInt
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vcristian.combus.post
import nl.booxchange.R
import nl.booxchange.model.entities.ChatModel
import nl.booxchange.model.events.ChatOpenedEvent
import nl.booxchange.model.events.ChatsStateChangeEvent
import nl.booxchange.utilities.database.FirebaseListQueryLiveData
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.recycler.ViewHolderConfig
import nl.booxchange.utilities.recycler.ViewHolderConfig.ViewType


class MessagesFragmentViewModel: BaseViewModel() {

    val activesList = MutableLiveData<List<ChatModel>>()
    val requestsList = MutableLiveData<List<ChatModel>>()

    val requestsViewsConfigurations = listOf<ViewHolderConfig<ChatModel>>(
        ViewHolderConfig(R.layout.list_item_request, ViewType.CHAT_REQUEST) { _, chatModel -> chatModel?.isRequest == true }
    )

    val chatsViewsConfigurations = listOf<ViewHolderConfig<ChatModel>>(
        ViewHolderConfig(R.layout.list_item_chat, ViewType.CHAT_ACTIVE) { _, chatModel -> chatModel?.isRequest == false }
    )

    init {
        FirebaseListQueryLiveData(FirebaseDatabase.getInstance().getReference("chats").orderByChild(FirebaseAuth.getInstance().currentUser?.uid!!).startAt(.0)).observeForever { list ->
            val unreads = list?.values?.sumBy { (it[FirebaseAuth.getInstance().currentUser?.uid] as? Long)?.toInt() ?: 0 } ?: 0

            list?.toList()?.map(ChatModel.Companion::fromFirebaseEntry).orEmpty().groupBy(ChatModel::isRequest).run {
                val requests = get(true).orEmpty()
                val actives = get(false).orEmpty()

                requestCount.set(requests.size)
                requestsList.postValue(requests)

                chatsCount.set(actives.size)
                unreadCount.set(unreads)
                activesList.postValue(actives)

                post(ChatsStateChangeEvent(requestCount.get() + chatsCount.get()))
            }
        }
    }

    val chatsCount = ObservableInt(0)
    val unreadCount = ObservableInt(0)
    val requestCount = ObservableInt(0)

    fun onChatItemClick(view: View, chatModel: ChatModel) {
        post(ChatOpenedEvent(chatModel))
    }
}
