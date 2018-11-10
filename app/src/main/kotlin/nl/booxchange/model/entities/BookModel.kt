package nl.booxchange.model.entities

import androidx.databinding.BaseObservable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import nl.booxchange.extension.toByte
import nl.booxchange.model.FirestoreObject


data class BookModel(
    @Exclude override var id: String = "",
    var userId: String = FirebaseAuth.getInstance().currentUser?.uid!!,
    var title: ObservableField<String> = ObservableField(""),
    var author: ObservableField<String> = ObservableField(""),
    var condition: ObservableInt = ObservableInt(0),
    var tradeBooks: List<String> = emptyList(),
    var info: ObservableField<String> = ObservableField(""),
    var edition: ObservableField<String> = ObservableField(""),
    var isbn: ObservableField<String> = ObservableField(""),
    var price: ObservableField<String> = ObservableField(""),
    var isSell: ObservableBoolean = ObservableBoolean(),
    var isTrade: ObservableBoolean = ObservableBoolean(),
    var images: List<String> = emptyList(),
    var views: Long = 0L,
    val timestamp: Long = 0L,
    val reference: DocumentSnapshot? = null
): BaseObservable(), FirestoreObject {

    val hasImages
        get() = images.isNotEmpty()

    fun equalsConditionLevel(level: Int): Boolean {
        return level == condition.get()
    }

    fun toFirestoreEntry(): Map<String, Any?> {
        return mapOf(
                "title" to title.get()!!.trim(),
                "author" to author.get()!!.trim(),
                "condition" to condition.get(),
//                "booksTrade" to tradeBooks.get()!!.trim(),
                "info" to info.get()!!.trim(),
                "userID" to userId,
                "edition" to edition.get()!!.toIntOrNull(),
                "isbn" to isbn.get()!!.replace("-", "").trim(),
                "price" to price.get()!!.toFloatOrNull(),
                "isSell" to isSell.get(),
                "isTrade" to isTrade.get()
        )
    }

    companion object {
        fun fromFirestoreEntry(entry: DocumentSnapshot): BookModel {
            val (key, value) = entry.id to (entry.data ?: emptyMap())
            return BookModel(
                    key,
                    value["userID"] as? String ?: "",
                    ObservableField(value["title"] as? String ?: ""),
                    ObservableField(value["author"] as? String ?: ""),
                    ObservableInt((value["condition"] as? Long)?.toInt() ?: 0),
                    (value["tradeBooks"] as? List<String>).orEmpty(),
                    ObservableField(value["info"] as? String ?: ""),
                    ObservableField((value["edition"] as? Long)?.toString() ?: ""),
                    ObservableField(value["isbn"] as? String ?: ""),
                    ObservableField((value["price"] as? Double)?.toString() ?: ""),
                    ObservableBoolean(value["isSell"] as? Boolean ?: false),
                    ObservableBoolean(value["isTrade"] as? Boolean ?: false),
                    (value["images"] as? List<String>).orEmpty(),
                    (value["views"] as? Long) ?: 0L,
                    (value["timestamp"] as? Long) ?: 0L,
                    entry
            )
        }
    }

    enum class OfferType {
        NONE, TRADE, SELL, BOTH;

        companion object {
            fun getByFilters(exchangeFilter: Boolean, purchaseFilter: Boolean) = OfferType.values()[purchaseFilter.toByte().shl(1) or exchangeFilter.toByte()]
        }

        val isExchange
            get() = this in listOf(TRADE, BOTH)

        val isSell
            get() = this in listOf(SELL, BOTH)

        val isBoth
            get() = this == BOTH
    }
}
