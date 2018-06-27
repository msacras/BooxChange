package nl.booxchange.model

import com.google.gson.annotations.SerializedName
import nl.booxchange.extension.hashCode
import nl.booxchange.utilities.UserData
import java.io.Serializable

/**
 * Created by Cristian Velinciuc on 3/8/18.
 */
data class BookModel(
    @SerializedName("book_id") override val id: String = "",
    @SerializedName("title") var title: String? = null,
    @SerializedName("author") var author: String? = null,
    @SerializedName("edition") var edition: String? = null,
    @SerializedName("state") var condition: Int? = null,
    @SerializedName("isbn") var isbn: String? = null,
    @SerializedName("info") var info: String? = null,
    @SerializedName("images") var images: List<String>? = null,
    @SerializedName("user_id") var userId: String? = null,
    @SerializedName("offer_price") var offerPrice: String? = null,
    @SerializedName("offer_type") var offerType: OfferType? = null
): Distinctive, Serializable {
    override fun equals(other: Any?) = (other as? Distinctive)?.id == id
    override fun hashCode() = hashCode

    fun getFirstImage() = (images ?: emptyList<String>().also(::images::set)).firstOrNull()

    fun getHasImages() = (images ?: emptyList<String>().also(::images::set)).isNotEmpty()

    fun equalsConditionLevel(level: Int) = level == condition

    fun isSell() = offerType?.isSell ?: false

    fun isExchange() = offerType?.isExchange ?: false

    fun setIsSell(value: Boolean) = ::offerType.set(OfferType.getByFilters(isExchange(), value))

    fun setIsExchange(value: Boolean) = ::offerType.set(OfferType.getByFilters(value, isSell()))
}
