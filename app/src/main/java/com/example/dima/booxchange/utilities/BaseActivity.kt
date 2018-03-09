package com.example.dima.booxchange.utilities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import com.example.dima.booxchange.R
import com.example.dima.booxchange.widget.SlidingNavigationLayout
import org.jetbrains.anko.find

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
open class BaseActivity: AppCompatActivity() {
  private val drawer: SlidingNavigationLayout? by lazy { find<SlidingNavigationLayout>(R.id.navigation_layout) }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    drawer?.setDrawerView(R.layout.drawer_layout)
  }

  override fun onBackPressed() {
    if (drawer?.isDrawerOpen == true) {
      drawer?.setDrawerOpen?.invoke(false)
    } else {
      super.onBackPressed()
    }
  }
}
