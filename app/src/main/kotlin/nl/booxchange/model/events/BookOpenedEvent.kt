package nl.booxchange.model.events

import nl.booxchange.model.entities.BookModel

data class BookOpenedEvent(
    val bookModel: BookModel? = null,
    val bookId: String = bookModel?.id ?: ""
)
