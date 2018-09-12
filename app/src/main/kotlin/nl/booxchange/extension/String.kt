package nl.booxchange.extension


val String.withExitSymbol
    get() = "+$this"

val String.digitsOnly
    get() = replace("[^\\d]".toRegex(), "")

val String.takeNotBlank
    get() = this.takeIf { it.isNotBlank() }

val String.firebaseStoragePath
    get() = "gs://booxchange-nl.appspot.com/images/$this"
