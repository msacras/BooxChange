package nl.booxchange.model.entities

import android.databinding.BaseObservable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import nl.booxchange.extension.takeNotBlank
import nl.booxchange.extension.toByte
import nl.booxchange.model.FirebaseObject


data class BookModel(
    @Exclude override val id: String = FirebaseDatabase.getInstance().getReference("books").push().key!!,
    var userId: String = FirebaseAuth.getInstance().currentUser?.uid!!,

    var title: ObservableField<String> = ObservableField(""),
    var author: ObservableField<String> = ObservableField(""),
    var condition: ObservableInt = ObservableInt(0),
    var info: ObservableField<String> = ObservableField(""),
    var edition: ObservableField<String> = ObservableField(""),
    var isbn: ObservableField<String> = ObservableField(""),
    var price: ObservableField<String> = ObservableField(""),
    var forSale: ObservableBoolean = ObservableBoolean(),
    var forExchange: ObservableBoolean = ObservableBoolean(),

    var mainImage: String = "",
    var views: Long = 0L
): BaseObservable(), FirebaseObject {

    val hasImages
        get() = mainImage.isBlank()

    fun equalsConditionLevel(level: Int): Boolean {
        return level == condition.get()
    }

    fun toFirebaseEntry(): Map<String, Any?> {
        return mapOf(
            "title" to title.get()!!.trim(),
            "author" to author.get()!!.trim(),
            "condition" to condition.get(),
            "info" to info.get()!!.trim(),
            "user" to userId,
            "edition" to edition.get()!!.toIntOrNull(),
            "isbn" to isbn.get()!!.replace("-", "").toLongOrNull(),
            "price" to price.get()!!.toFloatOrNull(),
            "sell" to forSale.get(),
            "exchange" to forExchange.get(),
            "views" to views
        )
    }

    companion object {
        fun fromFirebaseEntry(entry: Pair<String, Map<String, Any>>): BookModel {
            val (key, value) = entry
            return BookModel(
                key,
                value["user"] as? String ?: "",
                ObservableField(value["title"] as? String ?: ""),
                ObservableField(value["author"] as? String ?: ""),
                ObservableInt((value["condition"] as? Long)?.toInt() ?: 0),
                ObservableField(value["info"] as? String ?: ""),
                ObservableField((value["edition"] as? Long)?.toString() ?: ""),
                ObservableField((value["isbn"] as? Long)?.toString() ?: ""),
                ObservableField((value["price"] as? Long)?.toString() ?: (value["edition"] as? Double)?.let { "%.2f".format(it) } ?: ""),
                ObservableBoolean(value["sell"] as? Boolean ?: false),
                ObservableBoolean(value["exchange"] as? Boolean ?: false),
                (value["image"] as? String)?.takeNotBlank?.let { "books/$key/$it" } ?: "",
                value["views"] as? Long ?: 0L
            )
        }
    }

    enum class OfferType {
        NONE, EXCHANGE, SELL, BOTH;

        companion object {
            fun getByFilters(exchangeFilter: Boolean, purchaseFilter: Boolean) = OfferType.values()[purchaseFilter.toByte().shl(1) or exchangeFilter.toByte()]
        }

        val isExchange
            get() = this in listOf(EXCHANGE, BOTH)

        val isSell
            get() = this in listOf(SELL, BOTH)

        val isBoth
            get() = this == BOTH
    }
}
