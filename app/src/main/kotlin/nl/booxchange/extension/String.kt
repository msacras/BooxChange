package nl.booxchange.extension

/**
 * Created by Cristian Velinciuc on 3/21/18.
 */

val String.withExitSymbol
  get() = "+$this"

val String.digitsOnly
  get() = replace("[^\\d]".toRegex(), "")
