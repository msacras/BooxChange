package nl.booxchange.extension

import nl.booxchange.model.FirestoreObject

val Any.string
    get() = this.toString()

val FirestoreObject.hashCode
    get() = javaClass.declaredFields.map { it.isAccessible = true; it.get(this)?.hashCode() ?: 0 }.sum()
