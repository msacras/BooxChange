package nl.booxchange.extension

import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.v4.graphics.drawable.DrawableCompat
import nl.booxchange.BooxchangeApp

fun Drawable.setTintCompat(@ColorRes id: Int): Drawable {
  return this.apply {
      DrawableCompat.setTint(this, BooxchangeApp.context.getColorCompat(id))
  }
}
