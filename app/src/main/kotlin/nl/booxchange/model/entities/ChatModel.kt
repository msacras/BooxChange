package nl.booxchange.model.entities

import androidx.databinding.*
import android.text.Spannable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import nl.booxchange.BR
import nl.booxchange.extension.single
import nl.booxchange.extension.takeNotBlank
import nl.booxchange.model.FirestoreObject


class ChatModel(@Exclude override val id: String): BaseObservable(), FirestoreObject {
    class ChatUserDataModel(val id: String, val unread: Int, val firstName: String?, val lastName: String?, val photo: String?)

    private val users = ObservableArrayMap<String, ChatUserDataModel>()
    val message = ObservableField<MessageModel>()
    var isRequest = false

    init {
        users.addOnMapChangedCallback(object: ObservableMap.OnMapChangedCallback<ObservableArrayMap<String, ChatUserDataModel>, String, ChatUserDataModel>() {
            override fun onMapChanged(sender: ObservableArrayMap<String, ChatUserDataModel>?, key: String?) {
                this@ChatModel.notifyPropertyChanged(BR.title)
                this@ChatModel.notifyPropertyChanged(BR.image)
            }
        })
    }

    @Bindable
    fun getTitle(): String? {
        return (getUserOther()?.firstName ?: "Anonymous") + " " + (getUserOther()?.lastName ?: "")
    }

    @Bindable
    fun getImage(): String? {
        return getUserOther()?.photo
    }

    @Bindable
    fun getUnreadCount(): Int {
        return getUserSelf()?.unread ?: 0
    }

    fun getUserSelf(): ChatUserDataModel? {
        return users["self"]
    }

    fun getUserOther(): ChatUserDataModel? {
        return users["other"]
    }

    fun getFormattedLastMessage(): Spannable? {
        return message.get()?.formattedContent
    }

    companion object {
        fun fromFirebaseEntry(entry: DocumentSnapshot): ChatModel {
            val key = entry.id
            val data = entry.data.orEmpty()

            val lastMessageId = data["lastMessageId"] as String
            val usersList = (data["counters"] as? Map<String, Long>).orEmpty()
            val chatModel = ChatModel(key)

            usersList.forEach { (userId, userUnreadMessages) ->
                if (userId == FirebaseAuth.getInstance().currentUser?.uid) {
                    chatModel.users["self"] = ChatModel.ChatUserDataModel(userId, userUnreadMessages.toInt(), "", "", "")
                } else {
                    chatModel.isRequest = userUnreadMessages == -1L

                    FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnSuccessListener {
                        val userData = it.data ?: return@addOnSuccessListener
                        chatModel.users["other"] = ChatModel.ChatUserDataModel(userId, userUnreadMessages.toInt(), userData["first_name"] as? String, userData["last_name"] as? String, userData["image_url"] as? String)
                    }
                }
            }

            FirebaseFirestore.getInstance().collection("chats").document(key).collection("messages").document(lastMessageId).get().addOnSuccessListener {
                chatModel.message.set(MessageModel.fromFirebaseEntry(it))
            }

            return chatModel
        }
    }
}
