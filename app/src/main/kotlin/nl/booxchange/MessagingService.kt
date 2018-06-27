package nl.booxchange

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vcristian.combus.post
import nl.booxchange.extension.asObject
import nl.booxchange.model.MessageModel
import nl.booxchange.model.MessageReceivedEvent
import nl.booxchange.utilities.UserData

class MessagingService: FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        remoteMessage ?: return

        BooxchangeApp.mainActivityDelegate?.let {
            if (remoteMessage.data["receiver"] != UserData.Session.userId) return
            remoteMessage.data["content"]?.asObject<MessageModel>()?.let { message ->
                post(MessageReceivedEvent(message))
/*
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
*/
            }
        } ?: run {

        }
    }
}
