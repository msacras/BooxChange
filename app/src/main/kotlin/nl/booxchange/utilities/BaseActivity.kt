package nl.booxchange.utilities

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import nl.booxchange.R
import nl.booxchange.api.APIClient.RequestManager
import nl.booxchange.screens.LibraryActivity
import nl.booxchange.screens.MessagesActivity
import nl.booxchange.screens.OffersActivity
import nl.booxchange.screens.ProfileActivity
import nl.booxchange.widget.SlidingNavigationLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.drawer_layout.view.*
import org.jetbrains.anko.find
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
        FirebaseAuth.getInstance().signOut()
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
