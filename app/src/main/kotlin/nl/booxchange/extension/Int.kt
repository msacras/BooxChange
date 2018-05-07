package nl.booxchange.extension

val Int.ARGB
    get() = IntArray(4) { shr(8 * (3 - it)) and 0xFF }

val IntArray.color
    get() = (0..3).map { get(it).shl(8 * (3 - it)) }.sum()
