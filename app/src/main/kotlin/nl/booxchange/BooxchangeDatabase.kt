package nl.booxchange

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.TypeConverters
import nl.booxchange.model.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

@Database(entities = [
    MessageModel::class,
    ChatModel::class,
    UserModel::class,
    BookModel::class
], version = 4, exportSchema = false)
@TypeConverters(DatabaseConverters::class)
abstract class BooxchangeDatabase: RoomDatabase() {
    abstract fun messagesDao(): MessagesDAO
    abstract fun chatsDao(): ChatsDAO
    abstract fun usersDao(): UsersDAO
    abstract fun booksDao(): BooksDAO

    companion object {
        lateinit var instance: BooxchangeDatabase
    }
}
