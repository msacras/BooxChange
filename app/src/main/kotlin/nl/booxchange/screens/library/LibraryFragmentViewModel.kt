package nl.booxchange.screens.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import nl.booxchange.R
import nl.booxchange.model.BookItemHandler
import nl.booxchange.model.entities.BookModel
import nl.booxchange.model.entities.UserModel
import nl.booxchange.screens.book.BookDetailsActivity
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.database.FirestoreListQueryLiveData
import nl.booxchange.utilities.database.LiveList
import nl.booxchange.utilities.recycler.ViewHolderConfig
import nl.booxchange.utilities.recycler.ViewHolderConfig.ViewType
import org.jetbrains.anko.startActivity

class LibraryFragmentViewModel: BaseViewModel(), BookItemHandler {
    val booksViewsConfigurations = listOf<ViewHolderConfig<BookModel>>(
        ViewHolderConfig(R.layout.list_item_book, ViewType.BOOK)
//        ViewHolderConfig(R.layout.list_item_book, ViewType.PLACEHOLDER)
    )

    val userBooksCount = ObservableField<String>("")
    val userBooksList: LiveData<List<BookModel>>
    val userProfile = ObservableField<UserModel>()

    val libraryIsEmpty = ObservableBoolean()

    init {
        userBooksList = Transformations.map(FirestoreListQueryLiveData(FirebaseFirestore.getInstance().collection("books").whereEqualTo("userID", FirebaseAuth.getInstance().currentUser?.uid)), ::parseBooks)
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().currentUser?.uid!!).addSnapshotListener { documentSnapshot, _ ->
            userProfile.set(UserModel.fromFirestoreEntry(documentSnapshot!!))
        }
    }

    override fun View.onBookItemClick(bookModel: BookModel) {
        (context as? AppCompatActivity)?.startActivity<BookDetailsActivity>(BookDetailsActivity.KEY_BOOK_ID to bookModel.id)
    }

    fun View.addBook() {
        (context as? AppCompatActivity)?.startActivity<BookDetailsActivity>()
    }

    private fun parseBooks(list: List<DocumentSnapshot>): List<BookModel> {
        libraryIsEmpty.set(list.isEmpty())
        userBooksCount.set(list.size.toString())
        return list.map(BookModel.Companion::fromFirestoreEntry).reversed()
    }
}
