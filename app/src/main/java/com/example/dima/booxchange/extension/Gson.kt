package com.example.dima.booxchange.extension


import com.example.dima.booxchange.model.ResponseModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */

val json = Gson()
inline fun <reified T> String.asObject(): T? {
  return try { json.fromJson<T>(this, object: TypeToken<T>(){}.type) } catch (e: Exception) { null }
}

/*
inline fun <reified T> String.asObject(): ResponseModel<T>? {
  return json.fromJson<ResponseModel<T>>(this, object: TypeToken<ResponseModel<T>>(){}.type)
}
*/

fun Any.asJson(): String {
  return json.toJson(this)
}
