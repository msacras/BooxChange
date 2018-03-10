package com.example.dima.booxchange

import android.app.Application

/**
 * Created by msacras on 3/10/18.
 */
class BooxchangeApp: Application() {
  override fun onCreate() {
    super.onCreate()

    delegate = this
  }

  companion object {
    lateinit var delegate: BooxchangeApp
  }
}
