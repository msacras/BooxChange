package nl.booxchange.utilities

import android.content.Context
import android.graphics.Point
import android.view.Display
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import nl.booxchange.BooxchangeApp
import nl.booxchange.R
import com.github.kittinunf.fuel.core.FuelManager

/**
 * Created by Cristian Velinciuc on 3/10/18.
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
