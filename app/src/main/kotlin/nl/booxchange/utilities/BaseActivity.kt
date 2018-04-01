package nl.booxchange.utilities

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.drawer_layout.view.*
import nl.booxchange.R
import nl.booxchange.api.APIClient.RequestManager
import nl.booxchange.screens.*
import nl.booxchange.widget.SlidingNavigationLayout
import org.jetbrains.anko.findOptional
import org.jetbrains.anko.startActivity

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
open class BaseActivity: AppCompatActivity() {
  private val rootLayout: SlidingNavigationLayout? by lazy { findOptional<SlidingNavigationLayout>(R.id.root_layout) }
  protected val requestManager = RequestManager(this)

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    initializeDrawerLayout()
  }

  private fun initializeDrawerLayout() {
    rootLayout?.setDrawerView(R.layout.drawer_layout)?.let { view ->
      view.go_to_profile_button.setOnClickListener {
        startActivity<ProfileActivity>()
      }
      view.go_to_messages_button.setOnClickListener {
        startActivity<MessagesActivity>()
      }
      view.go_to_library_button.setOnClickListener {
        startActivity<LibraryActivity>()
      }
      view.go_to_offers_button.setOnClickListener {
        startActivity<OffersActivity>()
      }
      view.go_to_settings_button.setOnClickListener {

      }
      view.go_to_logout_button.setOnClickListener {
        UserData.Authentication.logout()
        startActivity<LaunchActivity>()
        finish()
      }
    }
  }

  protected fun showSnackbar(@StringRes message: Int) {
    rootLayout?.showSnackbar(message)
  }

  override fun onBackPressed() {
    if (rootLayout?.isDrawerOpen == true) {
      rootLayout?.setDrawerOpen?.invoke(false)
    } else {
      super.onBackPressed()
    }
  }
}
