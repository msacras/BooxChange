package nl.booxchange.extension

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import nl.booxchange.utilities.Tools

/**
 * Created by Cristian Velinciuc on 3/11/18.
 */

fun Context.getColorCompat(@ColorRes id: Int): Int {
  return ContextCompat.getColor(this, id)
}

fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable {
  return ContextCompat.getDrawable(this, id)!!
}

fun Drawable.setTintCompat(@ColorRes id: Int): Drawable {
  return this.apply {
    DrawableCompat.setTint(this, Tools.safeContext.getColorCompat(id))
  }
}
