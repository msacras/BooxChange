package nl.booxchange.model

import java.io.Serializable

/**
 * Created by Cristian Velinciuc on 3/8/18.
 */
data class BookModel (
  override val id: Int,
  var title: String?,
  var author: String?,
  var edition: Int?,
  var isbn: String?,
  var info: String?,
  var image: String?,
  var user_id: Int?,
  var offer_price: String?,
  var offer_name: String?,
  var f_name: String?,
  var l_name: String?,
  var email: String?,
  var university: String?,
  var study_programme: String?,
  var study_year: String?
): Distinctive(), Serializable
