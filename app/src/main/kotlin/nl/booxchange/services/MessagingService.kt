package nl.booxchange.services

import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.RemoteInput
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import nl.booxchange.BooxchangeApp
import nl.booxchange.R
import nl.booxchange.extension.firebaseStoragePath
import nl.booxchange.extension.getColorCompat
import nl.booxchange.model.entities.MessageModel
import nl.booxchange.model.entities.MessageModel.MessageType
import nl.booxchange.screens.MainFragmentActivity
import nl.booxchange.extension.value
import nl.booxchange.utilities.Constants
import org.jetbrains.anko.dip


class MessagingService: FirebaseMessagingService() {
    override fun onNewToken(token: String?) {
        FirebaseDatabase.getInstance().getReference("instances").child(token ?: return).setValue(FirebaseAuth.getInstance().currentUser?.uid ?: return)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data["receiver"] != FirebaseAuth.getInstance().currentUser?.uid) return

        when (remoteMessage.data["type"]) {
            "MESSAGE", "REQUEST" -> {
                val chatId = remoteMessage.data["chatId"] ?: return
                val messageId = remoteMessage.data["messageId"] ?: return
                val messageBody = remoteMessage.data["messageBody"] ?: return
                val messageType = remoteMessage.data["messageType"] ?: return
                val messageSenderId = remoteMessage.data["messageSenderId"] ?: return
                val messageSenderName = if (remoteMessage.data["type"] == "MESSAGE") remoteMessage.data["messageSenderName"] ?: return else applicationContext.getString(R.string.app_name)

                val messageModel = MessageModel(messageId, messageSenderId, messageBody, messageType)

                showMessageNotification(messageModel, messageSenderName, chatId, remoteMessage.data["type"] == "MESSAGE")
            }
        }
    }

    private fun showMessageNotification(messageModel: MessageModel, senderName: String, chatId: String, includeReplyAction: Boolean) {
        val chatFragmentIntent = Intent(this@MessagingService, MainFragmentActivity::class.java)
        chatFragmentIntent.putExtra(Constants.EXTRA_PARAM_TARGET_VIEW, Constants.FRAGMENT_CHAT)
        chatFragmentIntent.putExtra(Constants.EXTRA_PARAM_CHAT_ID, chatId)
        chatFragmentIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

        val openChatIntent = PendingIntent.getActivity(this@MessagingService, 0, chatFragmentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val replyInputIntent = PendingIntent.getBroadcast(this@MessagingService, chatId.hashCode(), getMessageReplyIntent(chatId), PendingIntent.FLAG_UPDATE_CURRENT)
        val remoteInput = RemoteInput.Builder(Constants.KEY_RESPONSE_ID).setLabel("Reply").build()

        val replyAction = NotificationCompat.Action.Builder(R.drawable.send, "Reply", replyInputIntent).addRemoteInput(remoteInput).build()

        val senderProfileImageDiameter = applicationContext.dip(32)
        val senderProfileImageStyling = RequestOptions.circleCropTransform()
        val senderProfileImageUrl = (FirebaseDatabase.getInstance().getReference("users").child(messageModel.userId).child("image").value as? String)
        val senderProfileImageBitmap = Glide.with(this@MessagingService).asBitmap().load(senderProfileImageUrl).apply(senderProfileImageStyling).submit(senderProfileImageDiameter, senderProfileImageDiameter).get()

        val notificationStyle = if (messageModel.type == MessageType.IMAGE.name) {
            val bitmap = Glide.with(this@MessagingService).asBitmap().load(messageModel.content.firebaseStoragePath).submit(500, 500).get()
            NotificationCompat.BigPictureStyle().bigPicture(bitmap).setSummaryText(senderName)
        } else {
            NotificationCompat.InboxStyle().setBigContentTitle(senderName).addLine(messageModel.content)
        }
        val notification = NotificationCompat.Builder(this@MessagingService, "booxchange.channel.messaging")
            .setLargeIcon(senderProfileImageBitmap)
            .setSmallIcon(R.drawable.logo_icon)
            .setStyle(notificationStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setColor(getColorCompat(R.color.springGreen))
            .setAutoCancel(true)
            .setWhen(messageModel.timestamp.millis)
            .setLights(0xFFDBA77F.toInt(), 500, 5000)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentTitle(senderName)
            .setContentText(messageModel.content)
            .setContentIntent(openChatIntent)
            .also { notification ->
                if (includeReplyAction) {
                    notification.addAction(replyAction)
                }
            }
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
