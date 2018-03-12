package nl.booxchange.model

/**
 * Created by Cristian Velinciuc on 3/10/18.
 */
enum class OfferType {
  EXCHANGE, SELL, BOTH, NONE;

  companion object {
    fun getByFilters(exchangeFilter: Boolean, purchaseFilter: Boolean): OfferType {
      return when {
        exchangeFilter && purchaseFilter -> BOTH
        !exchangeFilter && purchaseFilter -> SELL
        exchangeFilter && !purchaseFilter -> EXCHANGE
        else -> NONE
      }
    }
  }
}
