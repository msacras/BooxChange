package nl.booxchange.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import nl.booxchange.extension.takeNotBlank
import nl.booxchange.utilities.Constants
import org.joda.time.DateTime

class InlineReplyService: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val replyText = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(Constants.KEY_RESPONSE_ID) ?: return
        val chatId = intent.getStringExtra(Constants.KEY_CHAT_ID) ?: return
        val message = mapOf(
            "senderID" to (FirebaseAuth.getInstance().currentUser?.uid ?: return),
            "content" to (replyText.toString().takeNotBlank?.trim() ?: return),
            "type" to "TEXT",
            "timestamp" to DateTime.now().toString()
        )

        FirebaseDatabase.getInstance().getReference("messages/$chatId").child(DateTime.now().millis.toString()).setValue(message).addOnCompleteListener {
            NotificationManagerCompat.from(context).cancel(chatId.hashCode())
        }
    }
}
