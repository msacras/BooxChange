package nl.booxchange.screens

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import nl.booxchange.BooxchangeApp
import nl.booxchange.extension.asObject
import nl.booxchange.extension.toGone
import nl.booxchange.model.MessageModel
import nl.booxchange.utilities.Constants
import nl.booxchange.utilities.UserData
import nl.booxchange.widget.LoadingView
import org.jetbrains.anko.startActivity

/**
 * Created by Cristian Velinciuc on 3/12/18.
 */
class LaunchActivity: AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

      FirebaseAuth.getInstance().currentUser?.let {
          setContentView(LoadingView(this).apply { message = "Synchronizing"; toGone(); show() })
          UserData.Authentication.login { isLoggedIn ->
        if (isLoggedIn) {
            val notificationPayload = intent.extras.getString("content")
            notificationPayload?.let(::handleNotification) ?: startActivity<HomepageActivity>()
        } else {
          UserData.Authentication.logout()
          startActivity<SignInActivity>()
        }
        finish()
      }
    } ?: run {
      startActivity<SignInActivity>()
      finish()
    }
  }

    private fun handleNotification(content: String) {
        content.asObject<MessageModel>()?.let { message ->
            BooxchangeApp.delegate.activityStack.find { it is ChatActivity && it.chatModel.id == message.chatId }?.let {
                (it as ChatActivity).receiveMessage(message)
                startActivity(Intent(this, ChatActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) })
            } ?: run {
                startActivity<ChatActivity>(Constants.EXTRA_PARAM_CHAT_ID to message.chatId)
            }
        }
    }
}
