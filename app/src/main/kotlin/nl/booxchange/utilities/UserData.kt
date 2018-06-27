package nl.booxchange.utilities

import android.databinding.ObservableField
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import nl.booxchange.BooxchangeApp
import nl.booxchange.api.APIClient.Book
import nl.booxchange.api.APIClient.User
import nl.booxchange.extension.asJson
import nl.booxchange.extension.asObject
import nl.booxchange.model.BookModel
import nl.booxchange.model.UserModel
import org.jetbrains.anko.defaultSharedPreferences

/**
 * Created by Cristian Velinciuc on 3/22/18.
 */
object UserData {
    object Authentication {
        fun register(onCompleted: (isLoggedIn: Boolean, isNewUser: Boolean) -> Unit) {
            findUser {
                it?.let {
                    UserData.Session.userModel = it
                    onCompleted(true, false)
                } ?: registerUser {
                    it?.let {
                        UserData.Session.userModel = it
                        onCompleted(true, true)
                    } ?: onCompleted(false, false)
                }
            }
        }

        fun login(onCompleted: (isLoggedIn: Boolean) -> Unit) {
            findUser {
                UserData.Session.userModel = it
                onCompleted(it != null)
            }
        }

        fun logout() {
            FirebaseAuth.getInstance().signOut()
            LoginManager.getInstance().logOut()
            UserData.purge()
        }

        private fun findUser(onResult: (UserModel?) -> Unit) {
            User.userGet(Session.userId) {
                onResult(it)
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
        val userId
            get() = FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception("User not found")
        val instanceId
            get() = FirebaseInstanceId.getInstance().token ?: throw Exception("No id at this moment")

        var userModel: UserModel? = null
        var userBooks: List<BookModel> = emptyList()

        init {
            FirebaseAuth.getInstance().addAuthStateListener {
                FirebaseAuth.getInstance().currentUser?.let {
                    User.updateInstanceId {}
                }
            }
        }

        fun fetchUserBooksList(onCompleted: (success: Boolean) -> Unit) {
            Book.fetchBooksByUserId(userModel?.id ?: return) {
                userBooks = it ?: emptyList()
                onCompleted(it != null)
            }
        }

        fun purge() {
            userModel = null
            userBooks = emptyList()
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
