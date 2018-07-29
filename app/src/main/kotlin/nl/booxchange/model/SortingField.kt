package nl.booxchange.model

enum class SortingField {
    VIEWS, BOOK_ID;

    override fun toString(): String {
        return super.toString().toLowerCase()
    }
}
