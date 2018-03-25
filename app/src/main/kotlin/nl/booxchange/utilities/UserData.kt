package nl.booxchange.utilities

import android.support.v4.content.ContextCompat.startActivity
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import nl.booxchange.BooxchangeApp
import nl.booxchange.api.APIClient
import nl.booxchange.api.APIClient.RequestManager
import nl.booxchange.extension.asJson
import nl.booxchange.extension.asObject
import nl.booxchange.model.BookModel
import nl.booxchange.model.UserModel
import nl.booxchange.screens.HomepageActivity
import nl.booxchange.utilities.UserData.Session.userModel
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

/**
 * Created by Cristian Velinciuc on 3/22/18.
 */
object UserData {
  object Authentication {
    fun register(userId: String, onCompleted: (isLoggedIn: Boolean, isNewUser: Boolean) -> Unit) {
      findUser(userId) {
        it?.let {
          UserData.Session.userModel = it
          onCompleted(true, false)
        } ?: registerUser(userId) {
          it?.let {
            UserData.Session.userModel = it
            onCompleted(true, true)
          } ?: onCompleted(false, false)
        }
      }
    }

    fun login(userId: String, onCompleted: (isLoggedIn: Boolean) -> Unit) {
      findUser(userId) {
        it?.let {
          UserData.Session.userModel = it
          onCompleted(true)
        } ?: onCompleted(false)
      }
    }

    fun logout() {
      FirebaseAuth.getInstance().signOut()
      LoginManager.getInstance().logOut()
      UserData.purge()
    }

    private fun findUser(userId: String, onResult: (UserModel?) -> Unit) {
      RequestManager.instance.userGet(userId) {
        onResult(it?.result)
      }
    }

    private fun registerUser(userId: String, onResult: (UserModel?) -> Unit) {
      val userModel = UserModel(userId)
      RequestManager.instance.userAdd(userModel) {
        onResult(it?.result)
      }
    }
  }

  object Session {
    var userModel: UserModel? = null
    var userBooks: List<BookModel> = emptyList()

    fun fetchUserBooksList(onCompleted: (success: Boolean) -> Unit) {
      RequestManager.instance.fetchBooksByUserId(userModel?.id ?: return) {
        userBooks = it?.result ?: emptyList()
        onCompleted(it?.result != null)
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
        else -> preferences.putString(key, value.asJson())
      }

      preferences.apply()
    }

    fun <T: Any> read(key: String, type: Class<T>): T? {
      val preferences = BooxchangeApp.delegate.applicationContext.defaultSharedPreferences

      return takeIf { !preferences.contains(key) }?.let {
        when (type) {
          Int::class.java -> preferences.getInt(key, 0)
          Long::class.java -> preferences.getLong(key, 0L)
          Float::class.java -> preferences.getFloat(key, 0f)
          Double::class.java -> preferences.getFloat(key, 0f)
          String::class.java -> preferences.getString(key, null)
          Boolean::class.java -> preferences.getBoolean(key, false)
          else -> preferences.getString(key, null).asObject()
        } as? T?
      }
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
