package nl.booxchange.screens.home

import android.arch.lifecycle.LiveData
import android.databinding.ObservableField
import android.view.View
import com.google.firebase.database.FirebaseDatabase
import com.vcristian.combus.expect
import com.vcristian.combus.post
import nl.booxchange.R
import nl.booxchange.model.BookItemHandler
import nl.booxchange.model.BookModel
import nl.booxchange.model.BookOpenedEvent
import nl.booxchange.model.BooksListOpenedEvent
import nl.booxchange.screens.chat.FirebasePagingDataSource
import nl.booxchange.screens.chat.ListLiveData
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.ViewHolderConfig

class MoreFragmentViewModel: BaseViewModel(), BookItemHandler {
    override fun onBookItemClick(view: View, bookModel: BookModel) {
        post(BookOpenedEvent(bookModel))
    }

    //not used
    override val checkedBook = ObservableField<BookModel>()

    private val currentChatDatabaseReference = FirebaseDatabase.getInstance().getReference("books")
    private var booksListReference = currentChatDatabaseReference.limitToLast(1)

    private val dataSource = FirebasePagingDataSource(currentChatDatabaseReference, BookModel.Companion::fromFirebaseEntry)

    val booksList: LiveData<List<BookModel>> = ListLiveData()

    val booksViewsConfigurations = listOf<ViewHolderConfig<BookModel>>(
            ViewHolderConfig(R.layout.more_list_item_book, 0) { _, _ -> true }
    )

    init {
        (booksList as ListLiveData).postValue(null)

        expect(BooksListOpenedEvent::class.java) {
            booksListReference = when (it.type) {
                "SELL" -> FirebaseDatabase.getInstance().getReference("books").orderByChild("sell").equalTo(true)
                "TRADE" -> FirebaseDatabase.getInstance().getReference("books").orderByChild("exchange").equalTo(true)
                "VIEWS" -> FirebaseDatabase.getInstance().getReference("books").orderByChild("views")
                else -> FirebaseDatabase.getInstance().getReference("books")
            }

            dataSource.baseQuery = currentChatDatabaseReference
            booksList.postValue(null)
            dataSource.loadInitial(booksList::postValue)
        }
    }
}