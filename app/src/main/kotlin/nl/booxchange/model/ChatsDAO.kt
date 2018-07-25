package nl.booxchange.model

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.room.*
import nl.booxchange.BooxchangeDatabase

@Dao
abstract class ChatsDAO {
    @Query("SELECT * FROM chats")
    abstract fun getChats(): DataSource.Factory<Int, ChatModel>

    @Query("SELECT * FROM chats")
    abstract fun getRequests(): DataSource.Factory<Int, ChatModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertChats(vararg chats: ChatModel)

    @Update
    abstract fun updateChats(vararg chats: ChatModel)

    @Delete
    abstract fun deleteChats(vararg chats: ChatModel)

    companion object {
        @JvmStatic
        @TypeConverter
        fun messageModelToId(messageModel: MessageModel): String {
            return messageModel.id
        }

        @JvmStatic
        @TypeConverter
        fun messageIdToModel(messageId: String): MessageModel {
            return BooxchangeDatabase.instance.messagesDao().getMessage(messageId)
        }
    }
}
