package nl.booxchange.screens.home

import android.databinding.Observable
import android.databinding.ObservableField
import android.view.View
import com.vcristian.combus.post
import nl.booxchange.api.APIClient.Book
import nl.booxchange.model.*
import nl.booxchange.utilities.BaseViewModel
import kotlin.properties.Delegates

class HomeFragmentViewModel: BaseViewModel(), BookItemHandler {
    //Not used
    override val checkedBook = ObservableField<BookModel>()

    private var requestsSemaphore by Delegates.observable(0) { _, _, completionCount ->
        if (completionCount == 0) {
            onLoadingStarted()
        } else {
            currentListType?.let { if (completionCount == 3) onLoadingFinished() } ?: onLoadingFinished()
        }
    }
    private val requestsCompletionListener = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            requestsSemaphore++
        }
    }

    val topBooksList = ObservableField<List<BookModel>>()
    val latestExchangeList = ObservableField<List<BookModel>>()
    val latestSellList = ObservableField<List<BookModel>>()

    var currentListType: OfferType? = null

    init {
        listOf(topBooksList, latestExchangeList, latestSellList).forEach { it.addOnPropertyChangedCallback(requestsCompletionListener) }

        onRefresh()
    }

    override fun onRefresh() {
        requestsSemaphore = 0
        when (currentListType) {
            OfferType.BOTH -> fetchBooksByCriteria("", OfferType.BOTH, 0, SortingField.VIEWS, topBooksList)
            OfferType.EXCHANGE -> fetchBooksByCriteria("", OfferType.EXCHANGE, 0, SortingField.NONE, latestExchangeList)
            OfferType.SELL -> fetchBooksByCriteria("", OfferType.SELL, 0, SortingField.NONE, latestSellList)
            else -> {
                fetchBooksByCriteria("", OfferType.BOTH, 0, SortingField.VIEWS, topBooksList)
                fetchBooksByCriteria("", OfferType.EXCHANGE, 0, SortingField.NONE, latestExchangeList)
                fetchBooksByCriteria("", OfferType.SELL, 0, SortingField.NONE, latestSellList)
            }
        }
    }

    private fun fetchBooksByCriteria(queryKeyword: String, offerType: OfferType, startIndex: Int, sortingField: SortingField, receiver: ObservableField<List<BookModel>>) {
        Book.fetchAvailableBooks(queryKeyword, offerType, startIndex, sortingField, receiver::set)
    }

    override fun onBookItemClick(view: View, bookModel: BookModel) {
        post(BookOpenedEvent(bookModel))
    }
}
