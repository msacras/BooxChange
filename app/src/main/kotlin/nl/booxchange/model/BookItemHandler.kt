package nl.booxchange.model

import android.content.Intent
import android.databinding.ObservableField
import android.view.View
import nl.booxchange.screens.book.BookActivity

interface BookItemHandler {
    fun onBookItemClick(view: View, bookModel: BookModel) /*{
        val intent = Intent(view.context, BookActivity::class.java)
        view.context.startActivity(intent)
    }*/

    val checkedBook: ObservableField<BookModel>
}
