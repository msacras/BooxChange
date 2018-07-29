package nl.booxchange.model

data class BookOpenedEvent(
    val bookModel: BookModel? = null,
    val bookId: String = bookModel?.id!!
)
