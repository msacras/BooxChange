package nl.booxchange

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.RemoteInput
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vcristian.combus.post
import nl.booxchange.InlineReplyService.Companion.KEY_ACTION_REPLY
import nl.booxchange.InlineReplyService.Companion.KEY_CHAT_ID
import nl.booxchange.InlineReplyService.Companion.KEY_RESPONSE_ID
import nl.booxchange.api.APIClient
import nl.booxchange.extension.asObject
import nl.booxchange.extension.getColorCompat
import nl.booxchange.extension.staticResourceUrl
import nl.booxchange.model.MessageModel
import nl.booxchange.model.MessageReceivedEvent
import nl.booxchange.model.MessageType
import nl.booxchange.screens.MainFragmentActivity
import nl.booxchange.utilities.Constants
import nl.booxchange.utilities.Tools
import nl.booxchange.utilities.UserData
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime


class MessagingService: FirebaseMessagingService()
{
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        remoteMessage ?: return

        if (!UserData.Persistent.read("wants to receive notifications", true)) return

        if (BooxchangeApp.isInForeground) {
            if (remoteMessage.data["receiver"] != UserData.Session.userId) return
            remoteMessage.data["content"]?.asObject<MessageModel>()?.let { message ->
                post(MessageReceivedEvent(message))
            }
        } else {
            when (remoteMessage.data["type"]) {
                "message" -> {
                    val title = remoteMessage.data["title"] ?: return
                    val message = remoteMessage.data["content"]?.asObject<MessageModel>() ?: return

                    if (message.userId == UserData.Session.userId) return

                    showMessageNotification(title, message)
                }
            }
        }
    }

    private fun showMessageNotification(chatTitle: String, messageModel: MessageModel) {
        doAsync {
            val chatFragmentIntent = Intent(this@MessagingService, MainFragmentActivity::class.java).apply {
                putExtra(Constants.EXTRA_PARAM_TARGET_VIEW, Constants.FRAGMENT_CHAT)
                putExtra(Constants.EXTRA_PARAM_CHAT_ID, messageModel.chatId)
            }
            val openChatIntent = PendingIntent.getActivity(this@MessagingService, 0, chatFragmentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val replyInputIntent = PendingIntent.getBroadcast(this@MessagingService, messageModel.chatId.toInt(), getMessageReplyIntent(messageModel.chatId), PendingIntent.FLAG_UPDATE_CURRENT)
            val remoteInput = RemoteInput.Builder(KEY_RESPONSE_ID)
                .setLabel("Reply")
                .build()

            val replyAction = NotificationCompat.Action.Builder(R.drawable.send, "Reply", replyInputIntent)
                .addRemoteInput(remoteInput)
                .build()

            val notificationStyle = if (messageModel.type == MessageType.IMAGE) {
                val bitmap = Glide.with(this@MessagingService).asBitmap().load(messageModel.content.staticResourceUrl).submit(500, 500).get()
                NotificationCompat.BigPictureStyle().bigPicture(bitmap).setSummaryText(chatTitle)
            } else {
                NotificationCompat.InboxStyle().setBigContentTitle(chatTitle).addLine(messageModel.content)
            }

            val notification = NotificationCompat.Builder(this@MessagingService, "booxchange.messaging")
                .setSmallIcon(R.mipmap.ic_logo_48dp)
                .setStyle(notificationStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setChannelId("booxchange.channel.messaging")
                .addAction(replyAction)
                .setColor(getColorCompat(R.color.caramel))
                .setAutoCancel(true)
                .setWhen(messageModel.createdAt.millis)
                .setLights(0xDBA77F, 250, 150)
                .setContentIntent(openChatIntent)
                .build()

            uiThread {
                NotificationManagerCompat.from(this@MessagingService).notify(messageModel.chatId.toInt(), notification)
            }
        }
    }

    private fun getMessageReplyIntent(chatId: String): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent(this, InlineReplyService::class.java)
        } else {
            Intent(this, MainFragmentActivity::class.java)
        }.apply {
            action = KEY_ACTION_REPLY
            putExtra(KEY_CHAT_ID, chatId)
        }
    }
}

class InlineReplyService: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        RemoteInput.getResultsFromIntent(intent)?.let {
            val replyText = it.getCharSequence(KEY_RESPONSE_ID).toString()
            val chatId = intent.getStringExtra(KEY_CHAT_ID) ?: return@let

            APIClient.Chat.postMessage(MessageModel("", chatId, UserData.Session.userId, replyText, MessageType.TEXT, DateTime.now())) {
                it?.let { context.toast("Reply sent!") }
                NotificationManagerCompat.from(Tools.safeContext).cancel(chatId.toInt())
            }
        }
    }

    companion object {
        const val KEY_ACTION_REPLY = "KEY_ACTION_REPLY"
        const val KEY_RESPONSE_ID = "KEY_RESPONSE_ID"
        const val KEY_CHAT_ID = "KEY_CHAT_ID"
    }
}
