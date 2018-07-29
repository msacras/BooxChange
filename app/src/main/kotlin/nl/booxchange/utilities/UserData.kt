package nl.booxchange.utilities

import android.arch.lifecycle.LiveData
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import nl.booxchange.BooxchangeApp
import nl.booxchange.BooxchangeDatabase
import nl.booxchange.api.APIClient
import nl.booxchange.api.APIClient.User
import nl.booxchange.extension.asJson
import nl.booxchange.extension.asObject
import nl.booxchange.model.BookModel
import nl.booxchange.model.UserModel
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by Cristian Velinciuc on 3/22/18.
 */
object UserData {
    init {
        Session; Authentication; Persistent
    }

    object Authentication {
        fun register(onCompleted: (isLoggedIn: Boolean, isNewUser: Boolean) -> Unit) {
            findUser { hasConnected, userModel ->
                if (!hasConnected) {
                    onCompleted(false, false)
                    return@findUser
                }

                userModel?.let {
                    doAsync {
                        BooxchangeDatabase.instance.usersDao().insertUsers(userModel)
                        uiThread {
                            onCompleted(true, false)
                        }
                    }
                } ?: registerUser {
                    it?.let { userModel ->
                        doAsync {
                            BooxchangeDatabase.instance.usersDao().insertUsers(userModel)
                            uiThread {
                                onCompleted(true, true)
                            }
                        }
                        onCompleted(true, true)
                    } ?: onCompleted(false, false)
                }
            }
        }

        fun login(onCompleted: (isLoggedIn: Boolean) -> Unit) {
            findUser { _, userModel ->
                doAsync {
                    userModel?.let {
                        BooxchangeDatabase.instance.usersDao().insertUsers(userModel)
                    }
                    uiThread {
                        onCompleted(userModel != null)
                    }
                }
            }
        }

        fun logout() {
            FirebaseAuth.getInstance().signOut()
            LoginManager.getInstance().logOut()
            UserData.purge()
        }

        private fun findUser(onResult: (Boolean, UserModel?) -> Unit) {
            User.userGet {
                println("USER_MODEL_CACHE")
                println(Session.userModel.value)

                it?.let {
                    onResult(true, it.result)
                } ?: run {
                    onResult(false, Session.userModel.value)
                }
            }
        }

        private fun registerUser(onResult: (UserModel?) -> Unit) {
            val userModel = UserModel(Session.userId)
            User.userAdd(userModel) {
                onResult(it)
            }
        }
    }

    object Session {
        @JvmStatic
        val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception("User not found")
        var instanceId: String? = null

        lateinit var userModel: LiveData<UserModel>
        lateinit var userBooks: LiveData<List<BookModel>>

        init {
            FirebaseAuth.getInstance().addAuthStateListener {
                it.currentUser?.let {
                    userModel = BooxchangeDatabase.instance.usersDao().getUserById(it.uid)
                    userBooks = BooxchangeDatabase.instance.booksDao().getBooksByUserId(it.uid)
                    userModel.observeForever {}
                    userBooks.observeForever {}
                }
            }
        }

        fun purge() {
            //TODO: DROP DATABASE
/*
            userModel = null
            userBooks = emptyList()
*/
        }
    }

    object Persistent {
        fun <T: Any> write(key: String, value: T) {
            val preferences = BooxchangeApp.delegate.applicationContext.defaultSharedPreferences.edit()

            when (value) {
                is Int -> preferences.putInt(key, value)
                is Long -> preferences.putLong(key, value)
                is Float -> preferences.putFloat(key, value)
                is Double -> preferences.putFloat(key, value.toFloat())
                is String -> preferences.putString(key, value)
                is Boolean -> preferences.putBoolean(key, value)
                else -> preferences.putString(key, value.asJson)
            }

            preferences.apply()
        }

        fun <T: Any> read(key: String, default: T): T {
            val preferences = BooxchangeApp.delegate.applicationContext.defaultSharedPreferences

            return when (default) {
                is Int -> preferences.getInt(key, default)
                is Long -> preferences.getLong(key, default)
                is Float -> preferences.getFloat(key, default)
                is Double -> preferences.getFloat(key, default.toFloat()).toDouble()
                is String -> preferences.getString(key, default)
                is Boolean -> preferences.getBoolean(key, default)
                else -> preferences.getString(key, null)?.asObject() ?: default
            } as T
        }

        fun purge() {
            BooxchangeApp.delegate.applicationContext.defaultSharedPreferences.edit().clear().apply()
        }
    }

    fun purge() {
        Session.purge()
        Persistent.purge()
    }
}
