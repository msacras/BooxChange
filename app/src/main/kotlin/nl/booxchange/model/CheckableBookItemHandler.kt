package nl.booxchange.model

import android.databinding.ObservableField
import android.view.View
import nl.booxchange.model.entities.BookModel

interface CheckableBookItemHandler {
    fun onBookItemClick(view: View, bookModel: BookModel)
    val checkedBook: ObservableField<BookModel>
}
