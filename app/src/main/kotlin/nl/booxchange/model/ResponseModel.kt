package nl.booxchange.model

import nl.booxchange.extension.asObject

/**
 * Created by Cristian Velinciuc on 3/10/18.
 */
abstract class ResponseModel {
  val success: Boolean = false
  val message: String = ""
}
