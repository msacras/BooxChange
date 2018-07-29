package nl.booxchange.model

import android.arch.paging.DataSource
import android.arch.persistence.room.*

@Dao
abstract class MessagesDAO {
    @Query("SELECT * FROM messages WHERE chat_id = :chatId")
    abstract fun getMessagesByChat(chatId: String): DataSource.Factory<Int, MessageModel>

    @Query("SELECT * FROM messages WHERE message_id = :messageId")
    abstract fun getMessage(messageId: String): MessageModel

    @Insert
    abstract fun insertMessages(vararg messages: MessageModel)

    @Update
    abstract fun updateMessages(vararg messages: MessageModel)

    @Delete
    abstract fun deleteMessages(vararg messages: MessageModel)
}
