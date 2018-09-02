package nl.booxchange.extension

/**
 * Created by Cristian Velinciuc on 3/21/18.
 */

val String.withExitSymbol
    get() = "+$this"

val String.digitsOnly
    get() = replace("[^\\d]".toRegex(), "")

val String.takeNotBlank
    get() = this.takeIf { it.isNotBlank() }

val String.firebaseStoragePath
    get() = "gs://booxchange-nl.appspot.com/images/$this"
