package nl.booxchange.screens.library

import android.app.Activity
import android.app.PendingIntent.getActivity
import android.arch.lifecycle.Transformations
import android.content.Context
import android.content.Intent
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.support.v4.content.ContextCompat.startActivity
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vcristian.combus.post
import nl.booxchange.R
import nl.booxchange.model.BookItemHandler
import nl.booxchange.model.BookModel
import nl.booxchange.model.BookOpenedEvent
import nl.booxchange.model.StartActivity
import nl.booxchange.screens.MainFragmentActivity
import nl.booxchange.screens.book.BookActivity
import nl.booxchange.screens.home.FirebaseItemQueryLiveData
import nl.booxchange.screens.home.FirebaseListQueryLiveData
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.ViewHolderConfig

class LibraryFragmentViewModel: BaseViewModel(), BookItemHandler {
    //Not used
    override val checkedBook = ObservableField<BookModel>()

    val booksViewsConfigurations = listOf<ViewHolderConfig<BookModel>>(
            ViewHolderConfig(R.layout.list_item_book, 0) { _, _ -> true }
    )

    val userBooksList = Transformations.map(FirebaseListQueryLiveData(FirebaseDatabase.getInstance().getReference("books").orderByChild("user").equalTo(FirebaseAuth.getInstance().currentUser?.uid)), ::parseBooks)
    val userProfile = ObservableField<UserModel>()

    val libraryIsEmpty = ObservableBoolean()

    init {
        Transformations.map(FirebaseItemQueryLiveData(FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().currentUser?.uid!!)), UserModel.Companion::fromFirebaseEntry).observeForever(userProfile::set)
    }

    override fun onBookItemClick(view: View, bookModel: BookModel) {
        post(BookOpenedEvent(bookModel))
    }

    fun View.addBook() {
        post(BookOpenedEvent())
    }

    private fun parseBooks(list: Map<String, Map<String, Any>>): List<BookModel> {
        libraryIsEmpty.set(list.isEmpty())

        FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().currentUser?.uid!!).child("books").setValue(list.size)

        return list.toList().map(BookModel.Companion::fromFirebaseEntry).reversed()
    }
}
