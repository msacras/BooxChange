package nl.booxchange.model

import android.arch.persistence.room.*
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.text.Spannable
import com.google.gson.annotations.SerializedName
import nl.booxchange.BR
import nl.booxchange.BooxchangeDatabase
import nl.booxchange.utilities.MessageUtilities
import nl.booxchange.utilities.UserData
import java.io.Serializable


@Entity(tableName = "chats")
data class ChatModel(

    @PrimaryKey
    @ColumnInfo(name = "chat_id")
    @SerializedName("chat_id")
    override val id: String,

    @ColumnInfo(name = "chat_title")
    @SerializedName("chat_title")
    val chatTitle: String,

    @ColumnInfo(name = "last_message")
    @SerializedName("last_message")
    var lastMessage: MessageModel?,

    @ColumnInfo(name = "unread_count")
    @SerializedName("unread_count")
    var unreadCount: Int,

    @ColumnInfo(name = "is_active")
    @SerializedName("is_active")
    val isActive: Boolean

): Serializable, Distinctive {

    //    @ColumnInfo(name = "users_list")
    @Ignore
    @SerializedName("users_list")
    val usersList: List<UserModel> = emptyList()

    fun getFormattedLastMessage(): Spannable {
        return MessageUtilities.formatRequest(lastMessage?.content ?: "", usersList)
    }

    fun getUserPhotoIds(): List<String?> {
        return usersList.minus(UserData.Session.userModel.value!!).sortedWith(compareBy({ it.id != lastMessage?.userId }, { it.photo == null })).take(3).map(UserModel::photo)
    }
}
