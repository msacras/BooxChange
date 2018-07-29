package nl.booxchange.model

import android.arch.lifecycle.Transformations
import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverter
import com.google.gson.annotations.SerializedName
import nl.booxchange.extension.hashCode
import nl.booxchange.utilities.UserData
import java.io.Serializable

/**
 * Created by Cristian Velinciuc on 3/8/18.
 */

@Entity(tableName = "books")
data class BookModel(

    @PrimaryKey
    @SerializedName("book_id")
    @ColumnInfo(name = "book_id")
    override val id: String,

    @SerializedName("title")
    @ColumnInfo(name = "title")
    var title: String?,

    @SerializedName("author")
    @ColumnInfo(name = "author")
    var author: String?,

    @SerializedName("edition")
    @ColumnInfo(name = "edition")
    var edition: String?,

    @SerializedName("state")
    @ColumnInfo(name = "state")
    var condition: Int?,

    @SerializedName("isbn")
    @ColumnInfo(name = "isbn")
    var isbn: String?,

    @SerializedName("info")
    @ColumnInfo(name = "info")
    var info: String?,

    @SerializedName("images")
    @ColumnInfo(name = "images")
    var images: List<String>?,

    @SerializedName("user_id")
    @ColumnInfo(name = "user_id")
    var userId: String?,

    @SerializedName("offer_price")
    @ColumnInfo(name = "offer_price")
    var offerPrice: String?,

    @SerializedName("offer_type")
    @ColumnInfo(name = "offer_type")
    var offerType: OfferType?,

    @SerializedName("views")
    @ColumnInfo(name = "views")
    var views: Int?

): Serializable, Distinctive {
/*
    override fun equals(other: Any?) = (other as? Distinctive)?.id == id
    override fun hashCode() = hashCode
*/

    fun getFirstImage(): String? {
        return (images ?: emptyList<String>().also(::images::set)).firstOrNull()
    }

    fun getHasImages(): Boolean {
        return (images ?: emptyList<String>().also(::images::set)).isNotEmpty()
    }

    fun equalsConditionLevel(level: Int): Boolean {
        return level == condition
    }

    fun isSell(): Boolean {
        return offerType?.isSell ?: false
    }

    fun isExchange(): Boolean {
        return offerType?.isExchange ?: false
    }

    fun setIsSell(value: Boolean) {
        offerType = OfferType.getByFilters(isExchange(), value)
    }

    fun setIsExchange(value: Boolean) {
        offerType = OfferType.getByFilters(value, isSell())
    }

    companion object {
        val newEmptyBook get() = BookModel("", null, null, null, null, null, null, null, UserData.Session.userId, null, null, null)
    }
}
