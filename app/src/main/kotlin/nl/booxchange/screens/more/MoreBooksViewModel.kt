package nl.booxchange.screens.more

import android.arch.lifecycle.LiveData
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.database.FirebaseDatabase
import nl.booxchange.R
import nl.booxchange.model.BookItemHandler
import nl.booxchange.model.entities.BookModel
import nl.booxchange.model.events.BooksListOpenedEvent
import nl.booxchange.screens.book.BookDetailsActivity
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.database.FirebasePagingDataSource
import nl.booxchange.utilities.database.ListLiveData
import nl.booxchange.utilities.recycler.ViewHolderConfig
import nl.booxchange.utilities.recycler.ViewHolderConfig.ViewType
import org.jetbrains.anko.startActivity

class MoreBooksViewModel: BaseViewModel(), BookItemHandler {
    private val databaseReference = FirebaseDatabase.getInstance().getReference("books")
    private var booksListReference = databaseReference.limitToLast(1)

    private val dataSource = FirebasePagingDataSource(databaseReference, BookModel.Companion::fromFirebaseEntry)

    val booksList: LiveData<List<BookModel?>> = ListLiveData()

    val booksViewsConfigurations = listOf<ViewHolderConfig<BookModel>>(
        ViewHolderConfig(R.layout.list_item_book, ViewType.BOOK) { _, _ -> true }
    )

    fun initializeWithConfig(booksListConfig: BooksListOpenedEvent) {
        booksListReference = when (booksListConfig.type) {
            "SELL" -> FirebaseDatabase.getInstance().getReference("books").orderByChild("sell").equalTo(true)
            "TRADE" -> FirebaseDatabase.getInstance().getReference("books").orderByChild("exchange").equalTo(true)
            "VIEWS" -> FirebaseDatabase.getInstance().getReference("books").orderByChild("views")
            else -> throw Exception("Unknown list type")
        }

        dataSource.baseQuery = databaseReference
        (booksList as ListLiveData).postValue(null)
        dataSource.loadInitial(booksList::postValue)
    }

    override fun View.onBookItemClick(bookModel: BookModel) {
        (context as? AppCompatActivity)?.startActivity<BookDetailsActivity>(BookDetailsActivity.KEY_BOOK_MODEL to bookModel)
    }
}
