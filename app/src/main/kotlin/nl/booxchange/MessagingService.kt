package nl.booxchange

import android.app.Dialog
import android.arch.lifecycle.Lifecycle
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import nl.booxchange.extension.asObject
import nl.booxchange.model.MessageModel
import nl.booxchange.screens.ChatActivity

class MessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        remoteMessage ?: return

        remoteMessage.data["content"]?.asObject<MessageModel>()?.let { message ->
            BooxchangeApp.delegate.activityStack.find { it is ChatActivity && it.chatModel.id == message.chatId }?.let {
                (it as ChatActivity).receiveMessage(message)
            } ?: run {
                BooxchangeApp.delegate.activityStack.find { it.lifecycle.currentState == Lifecycle.State.RESUMED }?.let {
                    it.runOnUiThread {
                        Dialog(it).apply {
                            setTitle("new message")
                            show()
                        }
                    }
                }
            }
        }
    }
}
