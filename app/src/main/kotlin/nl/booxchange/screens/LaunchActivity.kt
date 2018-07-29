package nl.booxchange.screens

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import nl.booxchange.extension.toGone
import nl.booxchange.utilities.Constants
import nl.booxchange.utilities.UserData
//import nl.booxchange.widget.LoadingView
import org.jetbrains.anko.startActivity

/**
 * Created by Cristian Velinciuc on 3/12/18.
 */
class LaunchActivity: AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    FirebaseAuth.getInstance().currentUser?.let {
      UserData.Authentication.login { isLoggedIn ->
        if (isLoggedIn) {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
                if (it.isSuccessful) {
                    UserData.Session.instanceId = it.result.token
                }
                startActivity<MainFragmentActivity>()
            }
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
}
