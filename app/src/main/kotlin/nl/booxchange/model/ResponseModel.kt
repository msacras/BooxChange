package nl.booxchange.model

/**
 * Created by Cristian Velinciuc on 3/10/18.
 */
data class ResponseModel<T> (
  val success: Boolean,
  val message: String,
  val result: T?
)
