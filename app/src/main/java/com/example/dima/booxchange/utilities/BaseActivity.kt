package com.example.dima.booxchange.utilities

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.example.dima.booxchange.R
import com.example.dima.booxchange.api.APIClient.RequestManager
import com.example.dima.booxchange.screens.LibraryActivity
import com.example.dima.booxchange.screens.MessagesActivity
import com.example.dima.booxchange.screens.OffersActivity
import com.example.dima.booxchange.screens.ProfileActivity
import com.example.dima.booxchange.widget.SlidingNavigationLayout
import kotlinx.android.synthetic.main.drawer_layout.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
open class BaseActivity: AppCompatActivity() {
  private val drawer: SlidingNavigationLayout? by lazy { find<SlidingNavigationLayout>(R.id.navigation_layout) }
  protected val requestManager = RequestManager(this)

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    initializeDrawerLayout()
  }

  private fun initializeDrawerLayout() {
    drawer?.setDrawerView(R.layout.drawer_layout)
    go_to_profile_button.setOnClickListener {
      startActivity<ProfileActivity>()
    }
    go_to_messages_button.setOnClickListener {
      startActivity<MessagesActivity>()
    }
    go_to_library_button.setOnClickListener {
      startActivity<LibraryActivity>()
    }
    go_to_offers_button.setOnClickListener {
      startActivity<OffersActivity>()
    }
    go_to_settings_button.setOnClickListener {

    }
    go_to_logout_button.setOnClickListener {

    }
  }

  protected fun showErrorSnackbar(@StringRes message: Int) {
    Snackbar.make(drawer as CoordinatorLayout, message, Snackbar.LENGTH_SHORT).show()
  }

  override fun onBackPressed() {
    if (drawer?.isDrawerOpen == true) {
      drawer?.setDrawerOpen?.invoke(false)
    } else {
      super.onBackPressed()
    }
  }
}
