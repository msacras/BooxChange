package nl.booxchange.model.entities

import android.net.Uri
import androidx.databinding.BaseObservable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import nl.booxchange.model.FirestoreObject
import nl.booxchange.utilities.MessageUtilities
import org.joda.time.DateTime

class MessageModel(
    @Exclude override val id: String,
    val userId: String = FirebaseAuth.getInstance().currentUser?.uid!!,
    val content: String,
    val type: String,
    val orderKey: Long,
    val timestamp: DateTime = DateTime(orderKey)
): BaseObservable(), FirestoreObject {

    val formattedContent
        get() = MessageUtilities.getFormattedMessage(this, isOwnMessage)

    val formattedDateTime
        get() = timestamp.toString("HH:mm.ss dd MMM ''yy")

    val isOwnMessage
        get() = userId == FirebaseAuth.getInstance().currentUser?.uid

    val isImage
        get() = if (type == "IMAGE") "" else null

    companion object {
        fun fromFirebaseEntry(entry: DocumentSnapshot): MessageModel {
            val (key, value) = entry.id to (entry.data.orEmpty())
            return MessageModel(
                key,
                value["senderID"] as? String ?: "",
                value["content"] as? String ?: "",
                value["type"] as? String ?: "",
                value["timestamp"] as? Long ?: 0L
            )
        }
    }

    enum class MessageType {
        TEXT, IMAGE, REQUEST
    }
}
