package nl.booxchange.extension


import android.databinding.ObservableInt
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */

val json = GsonBuilder()
    .registerTypeAdapter(DateTime::class.java, JsonSerializer<DateTime> { src, _, _ -> JsonPrimitive(ISODateTimeFormat.dateTimeNoMillis().print(src)) })
    .registerTypeAdapter(DateTime::class.java, JsonDeserializer<DateTime> { json, _, _ -> DateTime.parse(json.asString) })
    .registerTypeAdapter(ObservableInt::class.java, JsonSerializer<ObservableInt> { src, _, _ -> JsonPrimitive(src.get()) })
    .registerTypeAdapter(ObservableInt::class.java, JsonDeserializer<ObservableInt> { json, _, _ -> ObservableInt(json.asInt) })
    .create()

val parser = JsonParser()

inline fun <reified T> String.asObject(): T? {
  return /*try {*/ json.fromJson<T>(this, object: TypeToken<T>(){}.type)// } catch (e: Exception) { null }
}

inline fun <reified T> JsonElement.asObject(): T? {
  return /*try {*/ json.fromJson<T>(this, object: TypeToken<T>(){}.type)// } catch (e: Exception) { null }
}

val Any.asJson
  get() = json.toJson(this)

val String.parseJson
  get() = parser.parse(this)

val String.parseArray
  get() = parseJson.asJsonArray

val String.parseObject
  get() = parseJson.asJsonObject

val String.parseResult
  get() = parseObject?.get("result")
