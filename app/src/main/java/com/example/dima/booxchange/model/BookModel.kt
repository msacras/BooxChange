package com.example.dima.booxchange.model

import java.io.Serializable

/**
 * Created by Cristian Velinciuc on 3/8/18.
 */
data class BookModel (
  val id: Int,
  var title: String?,
  var author: String?,
  var edition: Int?,
  var isbn: String?,
  var info: String?,
  var image: String?,
  var user_id: Int?,
  var offer_price: String?,
  var offer_name: String?
): Serializable
