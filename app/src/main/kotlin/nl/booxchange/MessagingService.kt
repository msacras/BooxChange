package nl.booxchange

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.RemoteInput
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import nl.booxchange.extension.firebaseStoragePath
import nl.booxchange.extension.getColorCompat
import nl.booxchange.model.MessageModel
import nl.booxchange.model.MessageType
import nl.booxchange.screens.MainFragmentActivity
import nl.booxchange.utilities.Constants
import org.joda.time.DateTime


class MessagingService: FirebaseMessagingService()
{
    override fun onNewToken(token: String?) {
        FirebaseDatabase.getInstance().getReference("instances").child(token ?: return).setValue(FirebaseAuth.getInstance().currentUser?.uid ?: return)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data["receiver"] != FirebaseAuth.getInstance().currentUser?.uid) return

        if (BooxchangeApp.isInForeground) {

        } else {
            when (remoteMessage.data["type"]) {
                "MESSAGE" -> {
                    val chatId = remoteMessage.data["chatId"] ?: return
                    val messageId = remoteMessage.data["messageId"] ?: return
                    val messageBody = remoteMessage.data["messageBody"] ?: return
                    val messageType = remoteMessage.data["messageType"] ?: return
                    val messageTimestamp = remoteMessage.data["messageTimestamp"] ?: return
                    val messageSenderId = remoteMessage.data["messageSenderId"] ?: return
                    val messageSenderName = remoteMessage.data["messageSenderName"] ?: return

                    val messageModel = MessageModel(messageId, messageSenderId, messageBody, messageType, DateTime.parse(messageTimestamp))

                    showMessageNotification(messageModel, messageSenderName, chatId)
                }
            }
        }
    }

    private fun showMessageNotification(messageModel: MessageModel, senderName: String, chatId: String) {
            val chatFragmentIntent = Intent(this@MessagingService, MainFragmentActivity::class.java).apply {
                putExtra(Constants.EXTRA_PARAM_TARGET_VIEW, Constants.FRAGMENT_CHAT)
                putExtra(Constants.EXTRA_PARAM_CHAT_ID, chatId)
            }
            val openChatIntent = PendingIntent.getActivity(this@MessagingService, 0, chatFragmentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val replyInputIntent = PendingIntent.getBroadcast(this@MessagingService, chatId.hashCode(), getMessageReplyIntent(chatId), PendingIntent.FLAG_UPDATE_CURRENT)
        val remoteInput = RemoteInput.Builder(Constants.KEY_RESPONSE_ID)
                .setLabel("Reply")
                .build()

            val replyAction = NotificationCompat.Action.Builder(R.drawable.send, "Reply", replyInputIntent)
                .addRemoteInput(remoteInput)
                .build()

        val notificationStyle = if (messageModel.type == MessageType.IMAGE.name) {
            val bitmap = Glide.with(this@MessagingService).asBitmap().load(messageModel.content.firebaseStoragePath).submit(500, 500).get()
            NotificationCompat.BigPictureStyle().bigPicture(bitmap).setSummaryText(senderName)
            } else {
            NotificationCompat.InboxStyle().setBigContentTitle(senderName).addLine(messageModel.content)
            }

        val notification = NotificationCompat.Builder(this@MessagingService, "booxchange.channel.messaging")
                .setSmallIcon(R.drawable.ic_booxchange_icon)
                .setStyle(notificationStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .addAction(replyAction)
//                .setSound(Uri.parse("android.resource://" + packageName + "/" + R.raw.notification))
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(getColorCompat(R.color.springGreen))
                .setAutoCancel(true)
            .setWhen(messageModel.timestamp.millis)
                .setLights(0xDBA77F, 250, 150)
                .setContentIntent(openChatIntent)
                .build()
        NotificationManagerCompat.from(this@MessagingService).notify(chatId.hashCode(), notification)
    }

    private fun getMessageReplyIntent(chatId: String): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent(this, InlineReplyService::class.java)
        } else {
            Intent(this, MainFragmentActivity::class.java)
        }.apply {
            action = Constants.KEY_ACTION_REPLY
            putExtra(Constants.KEY_CHAT_ID, chatId)
            putExtra(Constants.EXTRA_PARAM_TARGET_VIEW, Constants.FRAGMENT_CHAT)
        }
    }
}
