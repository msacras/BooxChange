package nl.booxchange

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.TypeConverters
import nl.booxchange.model.ChatModel
import nl.booxchange.model.ChatsDAO
import nl.booxchange.model.MessageModel
import nl.booxchange.model.MessagesDAO
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

@Database(entities = [
    MessageModel::class,
    ChatModel::class
], version = 1, exportSchema = false)
@TypeConverters(
    BooxchangeDatabase::class,
    MessagesDAO::class,
    ChatsDAO::class
)
abstract class BooxchangeDatabase: RoomDatabase() {
    abstract fun messagesDao(): MessagesDAO
    abstract fun chatsDao(): ChatsDAO

    companion object {
        lateinit var instance: BooxchangeDatabase

        @JvmStatic
        @TypeConverter
        fun dateTimeToString(dateTime: DateTime): String {
            return dateTime.toString("yyyy-MM-dd'T'HH:mm:ssZ")
        }

        @JvmStatic
        @TypeConverter
        fun dateTimeFromString(dateTime: String): DateTime {
            return DateTime.parse(dateTime, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ"))
        }
    }
}
