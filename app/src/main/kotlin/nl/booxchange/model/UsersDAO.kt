package nl.booxchange.model

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
abstract class UsersDAO {
    @Query("SELECT * FROM users WHERE user_id = :id")
    abstract fun getUserById(id: String): LiveData<UserModel>

    @Query("SELECT * FROM users WHERE user_id = :id")
    abstract fun getUserById1(id: String): UserModel

    @Query("SELECT * FROM users WHERE user_id IN (:ids)")
    abstract fun getUsersById(vararg ids: String): List<UserModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertUsers(vararg users: UserModel)

    @Update
    abstract fun updateUsers(vararg users: UserModel)

    @Delete
    abstract fun deleteUsers(vararg users: UserModel)
}
