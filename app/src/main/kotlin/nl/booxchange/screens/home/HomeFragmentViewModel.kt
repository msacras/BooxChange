package nl.booxchange.screens.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.databinding.ObservableField
import android.view.View
import com.google.firebase.database.FirebaseDatabase
import com.vcristian.combus.post
import nl.booxchange.R
import nl.booxchange.model.BookItemHandler
import nl.booxchange.model.BookModel
import nl.booxchange.model.BookOpenedEvent
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.ViewHolderConfig


class HomeFragmentViewModel: BaseViewModel(), BookItemHandler {
    //Not used
    override val checkedBook = ObservableField<BookModel>()

    val booksViewsConfigurations = listOf<ViewHolderConfig<BookModel>>(
        ViewHolderConfig(R.layout.list_item_book, 0) { _, _ -> true }
    )

    val topBooksList: LiveData<List<BookModel>>
    val latestExchangeList: LiveData<List<BookModel>>
    val latestSellList: LiveData<List<BookModel>>

    init {
        val booksFirebaseReference = FirebaseDatabase.getInstance().getReference("books")
        latestSellList = Transformations.map(FirebaseListQueryLiveData(booksFirebaseReference.orderByChild("sell").equalTo(true).limitToLast(15)), ::parseBooks)
        latestExchangeList = Transformations.map(FirebaseListQueryLiveData(booksFirebaseReference.orderByChild("exchange").equalTo(true).limitToLast(15)), ::parseBooks)
        topBooksList = Transformations.map(FirebaseListQueryLiveData(booksFirebaseReference.orderByChild("views").limitToLast(15)), ::parseBooks)
    }

    private fun parseBooks(list: Map<String, Map<String, Any>>): List<BookModel> {
        return list.map { BookModel.fromFirebaseEntry(it) }.reversed()
    }

    override fun onBookItemClick(view: View, bookModel: BookModel) {
        post(BookOpenedEvent(bookModel))
    }
}
