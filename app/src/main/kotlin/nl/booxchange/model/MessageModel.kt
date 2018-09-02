package nl.booxchange.model

import android.databinding.BaseObservable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import nl.booxchange.utilities.MessageUtilities
import org.joda.time.DateTime

class MessageModel(@Exclude override val id: String = FirebaseDatabase.getInstance().getReference("books").push().key!!, val userId: String = FirebaseAuth.getInstance().currentUser?.uid!!, val content: String, val type: String, val timestamp: DateTime = DateTime(id.toLong())): BaseObservable(), FirebaseObject {

    val formattedContent
        get() = MessageUtilities.getFormattedMessage(this, isOwnMessage)

    val formattedDateTime
        get() = timestamp.toString("HH:mm.ss dd MMM ''yy")

    val isOwnMessage
        get() = userId == FirebaseAuth.getInstance().currentUser?.uid

    val image
        get() = if (type == "IMAGE") "" else null

    companion object {
        fun fromFirebaseEntry(entry: Pair<String, Map<String, Any>>): MessageModel {
            val (key, value) = entry
            return MessageModel(
                key,
                value["user"] as? String ?: "",
                value["content"] as? String ?: "",
                value["type"] as? String ?: ""
            )
        }
    }
}
