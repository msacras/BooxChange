package nl.booxchange.screens.messages

import androidx.lifecycle.MutableLiveData
import androidx.databinding.ObservableInt
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vcristian.combus.post
import nl.booxchange.R
import nl.booxchange.model.entities.ChatModel
import nl.booxchange.model.events.ChatsStateChangeEvent
import nl.booxchange.screens.chat.ChatActivity
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.database.FirestoreListQueryLiveData
import nl.booxchange.utilities.recycler.ViewHolderConfig
import nl.booxchange.utilities.recycler.ViewHolderConfig.ViewType
import org.jetbrains.anko.startActivity


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
        val selfUID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        FirestoreListQueryLiveData(FirebaseFirestore.getInstance().collection("chats").whereArrayContains("usersIDs", selfUID)).observeForever { list ->
            val chatsByType = list.map(ChatModel.Companion::fromFirebaseEntry).groupBy(ChatModel::isRequest)

            val requests = chatsByType.get(true).orEmpty()
            val actives = chatsByType.get(false).orEmpty()
            val unread = actives.sumBy(ChatModel::getUnreadCount)

            requestCount.set(requests.size)
            requestsList.postValue(requests)

            chatsCount.set(actives.size)
            unreadCount.set(unread)
            activesList.postValue(actives)

            post(ChatsStateChangeEvent(requestCount.get() + chatsCount.get()))
        }
    }

    val chatsCount = ObservableInt(0)
    val unreadCount = ObservableInt(0)
    val requestCount = ObservableInt(0)

    fun onChatItemClick(view: View, chatModel: ChatModel) {
        view.context.startActivity<ChatActivity>(ChatActivity.KEY_CHAT_ID to chatModel.id)
    }
}
