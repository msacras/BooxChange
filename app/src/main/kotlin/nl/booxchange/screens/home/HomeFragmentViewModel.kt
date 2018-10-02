package nl.booxchange.screens.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.databinding.ObservableField
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.database.FirebaseDatabase
import nl.booxchange.R
import nl.booxchange.model.BookItemHandler
import nl.booxchange.model.entities.BookModel
import nl.booxchange.screens.book.BookDetailsActivity
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.database.FirebaseListQueryLiveData
import nl.booxchange.utilities.recycler.ViewHolderConfig
import nl.booxchange.utilities.recycler.ViewHolderConfig.ViewType
import org.jetbrains.anko.startActivity


class HomeFragmentViewModel: BaseViewModel(), BookItemHandler {
    val booksViewsConfigurations = listOf<ViewHolderConfig<BookModel>>(
        ViewHolderConfig(R.layout.list_item_book, ViewType.BOOK)
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
        return list.toList().map { BookModel.fromFirebaseEntry(it) }.reversed()
    }

    override fun View.onBookItemClick(bookModel: BookModel) {
        (context as? AppCompatActivity)?.startActivity<BookDetailsActivity>(BookDetailsActivity.KEY_BOOK_MODEL to bookModel)
    }
}
