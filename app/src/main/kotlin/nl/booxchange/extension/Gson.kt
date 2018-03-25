package nl.booxchange.extension


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */

val json = Gson()

inline fun <reified T> String.asObject(): T? {
  return try { json.fromJson<T>(this, object: TypeToken<T>(){}.type) } catch (e: Exception) { null }
}

fun Any.asJson(): String {
  return json.toJson(this)
}
