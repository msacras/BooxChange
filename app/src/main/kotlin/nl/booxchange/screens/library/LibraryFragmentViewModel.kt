package nl.booxchange.screens.library

import android.databinding.Observable
import android.databinding.ObservableField
import android.view.View
import com.vcristian.combus.post
import nl.booxchange.api.APIClient.Book
import nl.booxchange.model.*
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.UserData

class LibraryFragmentViewModel: BaseViewModel(), BookItemHandler {
    //Not used
    override val checkedBook = ObservableField<BookModel>()

    val userBooksList = ObservableField<List<BookModel>>()

    init {
        onRefresh()
    }

    override fun onRefresh() {
        fetchUserBooks()
    }

    private fun fetchUserBooks() {
        onLoadingStarted()
        Book.fetchBooksByUserId(UserData.Session.userId) {
            it?.let(userBooksList::set) ?: onLoadingFailed()
            onLoadingFinished()
        }
    }

    override fun onBookItemClick(view: View, bookModel: BookModel) {
        post(BookOpenedEvent(bookModel))
    }

    fun addBook(view: View) {
        post(BookOpenedEvent(BookModel.newEmptyBook))
    }
}
