package nl.booxchange.model

import nl.booxchange.extension.toByte

/**
 * Created by Cristian Velinciuc on 3/10/18.ksys
 */
enum class OfferType {
  NONE, EXCHANGE, SELL, BOTH;

  companion object {
    fun getByFilters(exchangeFilter: Boolean, purchaseFilter: Boolean): OfferType {
        return OfferType.values()[purchaseFilter.toByte().shl(1) or exchangeFilter.toByte()]
    }
  }

  val isExchange
    get() = this in listOf(EXCHANGE, BOTH)

  val isSell
    get() = this in listOf(SELL, BOTH)

  val isBoth
    get() = this == BOTH
}
