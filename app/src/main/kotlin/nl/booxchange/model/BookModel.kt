package nl.booxchange.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Cristian Velinciuc on 3/8/18.
 */
data class BookModel(
    @SerializedName("book_id") override val id: String,
    @SerializedName("title") var title: String? = null,
    @SerializedName("author") var author: String? = null,
    @SerializedName("edition") var edition: Int? = null,
    @SerializedName("condition") var condition: Int? = null,
    @SerializedName("isbn") var isbn: String? = null,
    @SerializedName("info") var info: String? = null,
    @SerializedName("image") var images: List<String>? = null,
    @SerializedName("user_id") var userId: String? = null,
    @SerializedName("offer_price") var offerPrice: String? = null,
    @SerializedName("offer_type") var offerType: String? = null
): Distinctive, Serializable {
    override fun equals(other: Any?) = (other as? Distinctive)?.id == id
    override fun hashCode() = javaClass.fields.map { it.get(this).hashCode() }.sum()
}
