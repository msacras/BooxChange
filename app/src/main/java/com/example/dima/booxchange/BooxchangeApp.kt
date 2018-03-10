package com.example.dima.booxchange

import android.app.Application
import com.example.dima.booxchange.api.APIClient

/**
 * Created by msacras on 3/10/18.
 */
class BooxchangeApp: Application() {
  override fun onCreate() {
    super.onCreate()

    APIClient
    delegate = this
  }

  companion object {
    lateinit var delegate: BooxchangeApp
  }
}
