package nl.booxchange.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Cristian Velinciuc on 3/8/18.
 */
data class BookModel (
    @SerializedName("book_id") override val id: String,
    @SerializedName("title") var title: String? = null,
    @SerializedName("author") var author: String? = null,
    @SerializedName("edition") var edition: Int? = null,
    @SerializedName("state") var state: Int? = null,
    @SerializedName("isbn") var isbn: String? = null,
    @SerializedName("info") var info: String? = null,
    @SerializedName("image") var image: String? = null,
    @SerializedName("user_id") var userId: String? = null,
    @SerializedName("offer_price") var offerPrice: String? = null,
    @SerializedName("offer_type") var offerType: String? = null
): Distinctive, Serializable {
  override fun equals(other: Any?): Boolean {
    return if (other is Distinctive) {
      this.id == other.id
    } else false
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + (title?.hashCode() ?: 0)
    result = 31 * result + (author?.hashCode() ?: 0)
    result = 31 * result + (edition ?: 0)
      result = 31 * result + (state ?: 0)
    result = 31 * result + (isbn?.hashCode() ?: 0)
    result = 31 * result + (info?.hashCode() ?: 0)
    result = 31 * result + (image?.hashCode() ?: 0)
    result = 31 * result + (userId?.hashCode() ?: 0)
    result = 31 * result + (offerPrice?.hashCode() ?: 0)
    result = 31 * result + (offerType?.hashCode() ?: 0)
    return result
  }
}
