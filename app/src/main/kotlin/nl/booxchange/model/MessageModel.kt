package nl.booxchange.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverter
import com.google.gson.annotations.SerializedName
import nl.booxchange.utilities.MessageUtilities
import org.joda.time.DateTime
import java.io.Serializable

@Entity(tableName = "messages")
data class MessageModel(

    @PrimaryKey
    @ColumnInfo(name = "message_id")
    @SerializedName("message_id")
    override val id: String,

    @ColumnInfo(name = "chat_id")
    @SerializedName("chat_id")
    val chatId: String,

    @ColumnInfo(name = "user_id")
    @SerializedName("user_id")
    val userId: String,

    @ColumnInfo(name = "content")
    @SerializedName("content")
    val content: String,

    @ColumnInfo(name = "type")
    @SerializedName("type")
    val type: MessageType,

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: DateTime

): Serializable, Distinctive {

    fun getFormattedDateTime(): String {
        return createdAt.toString("HH:mm d MMM")
    }

    fun getFormattedContent(): CharSequence {
        return MessageUtilities.getFormattedMessage(this)
    }

    fun getImage(): String? {
        return content.takeIf { type == MessageType.IMAGE }
    }
}
