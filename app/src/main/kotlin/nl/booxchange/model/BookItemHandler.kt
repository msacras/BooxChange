package nl.booxchange.model

import android.databinding.ObservableField
import android.view.View

interface BookItemHandler {
    fun onBookItemClick(view: View, bookModel: BookModel)
    val checkedBook: ObservableField<BookModel>
}
