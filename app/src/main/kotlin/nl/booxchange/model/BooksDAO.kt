package nl.booxchange.model

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
abstract class BooksDAO {
    @Query("SELECT * FROM books WHERE user_id = :id")
    abstract fun getBooksByUserId(id: String): LiveData<List<BookModel>>

    @Query("SELECT * FROM books")// WHERE offer_type IN (:offerTypes) ORDER BY :sortingField DESC")
    abstract fun getBooksByCriteria(/*offerTypes: List<OfferType>, sortingField: SortingField*/): LiveData<List<BookModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertBooks(vararg books: BookModel)

    @Update
    abstract fun updateBooks(vararg books: BookModel)

    @Delete
    abstract fun deleteBooks(vararg books: BookModel)
}
