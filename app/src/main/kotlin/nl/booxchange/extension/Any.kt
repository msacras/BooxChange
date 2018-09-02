package nl.booxchange.extension

import nl.booxchange.model.FirebaseObject

val Any.string
    get() = this.toString()

val FirebaseObject.hashCode
    get() = javaClass.declaredFields.map { it.isAccessible = true; it.get(this)?.hashCode() ?: 0 }.sum()
