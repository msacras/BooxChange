package com.example.dima.booxchange.extension

import android.content.Context
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat

/**
 * Created by Cristian Velinciuc on 3/11/18.
 */

fun Context.getColorById(@ColorRes id: Int): Int {
  return ContextCompat.getColor(this, id)
}
