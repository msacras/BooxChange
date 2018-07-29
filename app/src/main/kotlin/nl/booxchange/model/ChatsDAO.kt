package nl.booxchange.model

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.room.*
import nl.booxchange.BooxchangeDatabase

@Dao
abstract class ChatsDAO {
    @Query("SELECT * FROM chats WHERE is_active = 1")
    abstract fun getChats(): DataSource.Factory<Int, ChatModel>

    @Query("SELECT * FROM chats WHERE is_active = 0")
    abstract fun getRequests(): DataSource.Factory<Int, ChatModel>

    @Query("SELECT SUM(unread_count) FROM chats WHERE is_active = 1")
    abstract fun getUnreadMessagesCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM chats WHERE is_active = 0")
    abstract fun getChatRequestsCount(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertChats(vararg chats: ChatModel)

    @Update
    abstract fun updateChats(vararg chats: ChatModel)

    @Delete
    abstract fun deleteChats(vararg chats: ChatModel)
}
