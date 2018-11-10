package nl.booxchange.screens.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import nl.booxchange.R
import nl.booxchange.model.BookItemHandler
import nl.booxchange.model.entities.BookModel
import nl.booxchange.screens.book.BookDetailsActivity
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.database.FirebaseListQueryLiveData
import nl.booxchange.utilities.database.FirestoreListQueryLiveData
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

    val noTopBooksVisibility = ObservableInt(View.GONE)
    val noLatestExchangeVisibility = ObservableInt(View.GONE)
    val noLatestSellVisibility = ObservableInt(View.GONE)

    init {
        val booksFirebaseReference = FirebaseFirestore.getInstance().collection("books")

        topBooksList = Transformations.map(FirestoreListQueryLiveData(booksFirebaseReference.orderBy("views", Query.Direction.DESCENDING).limit(15))) {
            parseBooks(it).also { noTopBooksVisibility.set(if (it.isEmpty()) View.VISIBLE else View.GONE) }
        }
        latestExchangeList = Transformations.map(FirestoreListQueryLiveData(booksFirebaseReference.whereEqualTo("isTrade", true).limit(15))) {
            parseBooks(it).also { noLatestExchangeVisibility.set(if (it.isEmpty()) View.VISIBLE else View.GONE) }
        }
        latestSellList = Transformations.map(FirestoreListQueryLiveData(booksFirebaseReference.whereEqualTo("isSell", true).limit(15))) {
            parseBooks(it).also { noLatestSellVisibility.set(if (it.isEmpty()) View.VISIBLE else View.GONE) }
        }
    }

    private fun parseBooks(list: List<DocumentSnapshot>): List<BookModel> {
        return list.map(BookModel.Companion::fromFirestoreEntry)
    }

    override fun View.onBookItemClick(bookModel: BookModel) {
        (context as? AppCompatActivity)?.startActivity<BookDetailsActivity>(BookDetailsActivity.KEY_BOOK_ID to bookModel.id)
    }
}
