package nl.booxchange.screens.library

import android.arch.lifecycle.Transformations
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vcristian.combus.post
import nl.booxchange.R
import nl.booxchange.model.BookItemHandler
import nl.booxchange.model.entities.BookModel
import nl.booxchange.model.entities.UserModel
import nl.booxchange.model.events.BookOpenedEvent
import nl.booxchange.screens.book.BookDetailsActivity
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.database.FirebaseItemQueryLiveData
import nl.booxchange.utilities.database.FirebaseListQueryLiveData
import nl.booxchange.utilities.recycler.ViewHolderConfig
import nl.booxchange.utilities.recycler.ViewHolderConfig.ViewType
import org.jetbrains.anko.startActivity

class LibraryFragmentViewModel: BaseViewModel(), BookItemHandler {
    val booksViewsConfigurations = listOf<ViewHolderConfig<BookModel>>(
        ViewHolderConfig(R.layout.list_item_book, ViewType.BOOK)
    )

    val userBooksList = Transformations.map(FirebaseListQueryLiveData(FirebaseDatabase.getInstance().getReference("books").orderByChild("user").equalTo(FirebaseAuth.getInstance().currentUser?.uid)), ::parseBooks)
    val userProfile = ObservableField<UserModel>()

    val libraryIsEmpty = ObservableBoolean()

    init {
        Transformations.map(FirebaseItemQueryLiveData(FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().currentUser?.uid!!)), UserModel.Companion::fromFirebaseEntry).observeForever(userProfile::set)
    }

    override fun View.onBookItemClick(bookModel: BookModel) {
        (context as? AppCompatActivity)?.startActivity<BookDetailsActivity>(BookDetailsActivity.KEY_BOOK_MODEL to bookModel)
    }

    fun View.addBook() {
        (context as? AppCompatActivity)?.startActivity<BookDetailsActivity>()
    }

    private fun parseBooks(list: Map<String, Map<String, Any>>): List<BookModel> {
        libraryIsEmpty.set(list.isEmpty())

        return list.toList().map(BookModel.Companion::fromFirebaseEntry).reversed()
    }
}
