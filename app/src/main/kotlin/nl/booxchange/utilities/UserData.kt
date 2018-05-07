package nl.booxchange.utilities

import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import nl.booxchange.BooxchangeApp
import nl.booxchange.api.APIClient.RequestManager
import nl.booxchange.extension.asJson
import nl.booxchange.extension.asObject
import nl.booxchange.model.BookModel
import nl.booxchange.model.UserModel
import org.jetbrains.anko.defaultSharedPreferences
import kotlin.reflect.KClass

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

      private fun findUser(onResult: (UserModel?) -> Unit) {
          RequestManager.instance.userGet(Session.userId) {
        onResult(it?.result)
      }
    }

      private fun registerUser(onResult: (UserModel?) -> Unit) {
          val userModel = UserModel(Session.userId)
      RequestManager.instance.userAdd(userModel) {
        onResult(it?.result)
      }
    }
  }

  object Session {
      val userId
          get() = FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception("User not found")
      val instanceId
          get() = FirebaseInstanceId.getInstance().token ?: throw Exception("No id at this moment")

    var userModel: UserModel? = null
    var userBooks: List<BookModel> = emptyList()

      init {
          FirebaseAuth.getInstance().addAuthStateListener {
              FirebaseAuth.getInstance().currentUser?.let {
                  RequestManager.instance.updateInstanceId {}
              }
          }
      }

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

      fun <T : Any> read(key: String, type: KClass<T>): T? {
      val preferences = BooxchangeApp.delegate.applicationContext.defaultSharedPreferences

      return takeIf { !preferences.contains(key) }?.let {
        when (type) {
            Int::class -> preferences.getInt(key, 0)
            Long::class -> preferences.getLong(key, 0L)
            Float::class -> preferences.getFloat(key, 0f)
            Double::class -> preferences.getFloat(key, 0f)
            String::class -> preferences.getString(key, null)
            Boolean::class -> preferences.getBoolean(key, false)
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
