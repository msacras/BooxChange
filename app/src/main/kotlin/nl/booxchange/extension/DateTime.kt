package nl.booxchange.extension

import org.joda.time.DateTime

val DateTime.serverTimestamp
    get() = this.toString("yyyy-MM-dd HH:mm:ss")
