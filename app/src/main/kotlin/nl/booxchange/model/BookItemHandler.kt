package nl.booxchange.model

import androidx.databinding.ObservableField
import android.view.View
import nl.booxchange.model.entities.BookModel

interface BookItemHandler {
    fun View.onBookItemClick(bookModel: BookModel)
}
