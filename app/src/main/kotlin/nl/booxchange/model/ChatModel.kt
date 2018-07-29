package nl.booxchange.model

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.text.Spannable
import com.google.gson.annotations.SerializedName
import nl.booxchange.BR
import nl.booxchange.utilities.MessageUtilities
import nl.booxchange.utilities.UserData
import java.io.Serializable

class ChatModel: BaseObservable(), Distinctive, Serializable {

    @SerializedName("chat_id")
    override val id: String = ""

    @SerializedName("chat_title")
    val chatTitle: String = ""

    @SerializedName("users_list")
    val usersList: List<UserModel> = emptyList()

    @Bindable
    @SerializedName("last_message")
    var lastMessage: MessageModel? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.lastMessage)
        }

    @get:Bindable
    @SerializedName("unread_count")
    var unreadCount: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.unreadCount)
        }

    @Bindable("lastMessage")
    fun getFormattedLastMessage(): Spannable {
        return MessageUtilities.formatRequest(lastMessage?.content ?: "", usersList)
    }

    fun getUserPhotoIds(): List<String?> {
        return usersList.minus(UserData.Session.userModel!!).sortedWith(compareBy({ it.id != lastMessage?.userId }, { it.photo == null })).take(3).map(UserModel::photo)
    }
}
