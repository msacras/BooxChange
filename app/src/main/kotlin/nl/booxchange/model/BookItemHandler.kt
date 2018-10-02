package nl.booxchange.model

import android.databinding.ObservableField
import android.view.View
import nl.booxchange.model.entities.BookModel

interface BookItemHandler {
    fun View.onBookItemClick(bookModel: BookModel)
}
