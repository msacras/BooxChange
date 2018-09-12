package nl.booxchange.model.entities

import android.databinding.*
import android.text.Spannable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import nl.booxchange.BR
import nl.booxchange.extension.single
import nl.booxchange.model.FirebaseObject


class ChatModel(@Exclude override val id: String): BaseObservable(), FirebaseObject {
    class ChatUserDataModel(val id: String, val unread: Int, val name: String?, val photo: String?)

    val users = ObservableArrayMap<String, ChatUserDataModel>()
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
        return getUserOther()?.name
    }

    @Bindable
    fun getImage(): String? {
        return getUserOther()?.photo
    }

    @Bindable
    fun getUnreadCount(): Int {
        return getUserSelf()?.unread ?: 0
    }

    private fun getUserSelf(): ChatUserDataModel? {
        return users["self"]
    }

    private fun getUserOther(): ChatUserDataModel? {
        return users["other"]
    }

    fun getFormattedLastMessage(): Spannable? {
        return message.get()?.formattedContent
    }

    companion object {
        fun fromFirebaseEntry(entry: Pair<String, Map<String, Any>>): ChatModel {
            val (key, value) = entry
            val lastMessageId = value["lastMessageId"] as String
            val usersList = value.filter { it.key !in listOf("isRequest", "lastMessageId") }
            val chatModel = ChatModel(key)

            usersList.forEach { (userId, userUnreadMessages) ->
                val userType: String

                if (userId == FirebaseAuth.getInstance().currentUser?.uid) {
                    userType = "self"
                } else {
                    chatModel.isRequest = userUnreadMessages == -1L
                    userType = "other"
                }
                FirebaseDatabase.getInstance().getReference("users").child(userId).single {
                    it?.let { (_, userData) ->
                        chatModel.users[userType] = ChatUserDataModel(userId, (userUnreadMessages as Long).toInt(), userData["alias"] as? String, userData["imageUrl"] as? String)
                    }
                }
            }

            FirebaseDatabase.getInstance().getReference("messages/$key").child(lastMessageId).single {
                it?.let { messageData ->
                    chatModel.message.set(MessageModel.fromFirebaseEntry(messageData))
                }
            }

            return chatModel
        }
    }
}
