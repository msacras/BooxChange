package nl.booxchange.extension

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat

fun Context.getColorCompat(@ColorRes id: Int): Int {
  return ContextCompat.getColor(this, id)
}

fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable {
  return ContextCompat.getDrawable(this, id)!!
}
