package nl.booxchange.screens.more

import androidx.lifecycle.LiveData
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import nl.booxchange.R
import nl.booxchange.model.BookItemHandler
import nl.booxchange.model.entities.BookModel
import nl.booxchange.screens.book.BookDetailsActivity
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.database.BooksPagingDataSource
import nl.booxchange.utilities.database.LiveList
import nl.booxchange.utilities.recycler.ViewHolderConfig
import nl.booxchange.utilities.recycler.ViewHolderConfig.ViewType
import org.jetbrains.anko.startActivity

class MoreBooksViewModel: BaseViewModel(), BookItemHandler {
    private lateinit var databaseReference: Query

    private val dataSource by lazy { BooksPagingDataSource(databaseReference, BookModel.Companion::fromFirestoreEntry) }

    val booksList: LiveData<List<BookModel?>> = LiveList()
    val searchQuery = ObservableField<String>()

    val booksViewsConfigurations = listOf<ViewHolderConfig<BookModel>>(
        ViewHolderConfig(R.layout.more_list_item_book, ViewType.BOOK) { _, _ -> true }
    )

    init {
        searchQuery.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                dataSource.filteringString = searchQuery.get() ?: ""
                if (dataSource.filteringString.length > 2) {
                    (booksList as LiveList).postValue(null)
                    dataSource.loadNext(null, booksList::postValue)
                }
            }
        })
    }

    fun initializeWithConfig(listType: String) {
        databaseReference = when (listType) {
            "SELL" -> FirebaseFirestore.getInstance().collection("books").whereEqualTo("isSell", true)
            "TRADE" -> FirebaseFirestore.getInstance().collection("books").whereEqualTo("isTrade", true)
            "VIEWS" -> FirebaseFirestore.getInstance().collection("books").orderBy("views", Query.Direction.DESCENDING)
            else -> throw Exception("Unknown list type")
        }

        dataSource.loadNext(null, (booksList as LiveList)::postValue)
    }

    fun fetchMoreBooks() {
        dataSource.loadNext(booksList.value?.last()?.reference, (booksList as LiveList)::plusAssign)
    }

    override fun View.onBookItemClick(bookModel: BookModel) {
        (context as? AppCompatActivity)?.startActivity<BookDetailsActivity>(BookDetailsActivity.KEY_BOOK_ID to bookModel.id)
    }
}
