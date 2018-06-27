package nl.booxchange.model

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import java.io.Serializable

data class MessageModel(
    @SerializedName("message_id") override val id: String,
    @SerializedName("chat_id") val chatId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("content") val content: String,
    @SerializedName("type") val type: MessageType,
    @SerializedName("created_at") val createdAt: DateTime
): Serializable, Distinctive {
    fun getFormattedDateTime() = createdAt.toString("HH:mm d MMM")
}
