package nl.booxchange.screens

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.startActivity

/**
 * Created by Cristian Velinciuc on 3/12/18.
 */
class LaunchActivity: AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    FirebaseAuth.getInstance().currentUser?.let {
      startActivity<HomepageActivity>()
    } ?: startActivity<SignInActivity>()
    finish()
  }
}
