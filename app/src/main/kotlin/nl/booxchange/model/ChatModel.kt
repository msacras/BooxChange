package nl.booxchange.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ChatModel(
    @SerializedName("chat_id") override val id: String,
    @SerializedName("chat_topic") val topic: String,
    @SerializedName("users_list") val usersList: List<UserModel>,
    @SerializedName("last_message") val lastMessage: MessageModel
) : Distinctive, Serializable
