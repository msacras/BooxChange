package com.example.dima.booxchange.utilities

import android.content.Context
import android.graphics.Point
import android.view.Display
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.dima.booxchange.BooxchangeApp
import com.example.dima.booxchange.R
import com.github.kittinunf.fuel.core.FuelManager

/**
 * Created by msacras on 3/10/18.
 */
object Tools {
  val safeContext
    get() = BooxchangeApp.delegate.applicationContext

  fun initializeImage(imageView: ImageView, url: String?) {
    try {
      Glide.with(safeContext).load(FuelManager.instance.basePath + "/images/$url").into(imageView)
    } catch (e: Exception) { /* Activity was destroyed before load could start */ }
  }

}
