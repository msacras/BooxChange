package nl.booxchange.extension

import nl.booxchange.model.Distinctive

val Any.string
    get() = this.toString()

val Distinctive.hashCode
    get() = javaClass.declaredFields.map { it.isAccessible = true; it.get(this)?.hashCode() ?: 0 }.sum()
