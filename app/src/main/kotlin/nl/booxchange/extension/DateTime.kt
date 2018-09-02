import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

val DateTime.isoDateTimeString get() = ISODateTimeFormat.dateTimeNoMillis().print(this)
