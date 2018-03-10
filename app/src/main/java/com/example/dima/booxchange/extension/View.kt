package com.example.dima.booxchange.extension

import android.view.View

/**
 * Created by msacras on 3/10/18.
 */
fun View.toVisible() {
  this.visibility = View.VISIBLE
}

fun View.toInvisible() {
  this.visibility = View.INVISIBLE
}

fun View.toGone() {
  this.visibility = View.GONE
}
