package nl.booxchange.extension

import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.core.graphics.drawable.DrawableCompat
import nl.booxchange.BooxchangeApp

fun Drawable.setTintCompat(@ColorRes id: Int): Drawable {
  return this.apply {
      DrawableCompat.setTint(this, BooxchangeApp.context.getColorCompat(id))
  }
}
