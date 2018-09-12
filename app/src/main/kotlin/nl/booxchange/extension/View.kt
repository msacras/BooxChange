package nl.booxchange.extension

import android.view.View


fun View.toVisible() {
  this.visibility = View.VISIBLE
}

fun View.toInvisible() {
  this.visibility = View.INVISIBLE
}

fun View.toGone() {
  this.visibility = View.GONE
}

fun View.setVisible(isVisible: Boolean) {
  if (isVisible) toVisible() else toGone()
}

val View.isVisible
  get() = this.visibility == View.VISIBLE

val View.isInvisible
  get() = this.visibility == View.INVISIBLE

val View.isGone
  get() = this.visibility == View.GONE
