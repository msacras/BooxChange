package nl.booxchange.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MessageModel(
    @SerializedName("message_id") override val id: String,
    @SerializedName("chat_id") val chatId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("content") val content: String,
    @SerializedName("type") val type: String,
    @SerializedName("created_at") val createdAt: String
) : Serializable, Distinctive
