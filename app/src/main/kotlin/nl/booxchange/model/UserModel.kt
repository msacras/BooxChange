package nl.booxchange.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserModel(
  @SerializedName("user_id") override val id: String,
  @SerializedName("facebook_id") var facebookId: String? = null,
  @SerializedName("google_id") var googleId: String? = null,
  @SerializedName("phone_id") var phoneId: String? = null,
  @SerializedName("first_name") var firstName: String? = null,
  @SerializedName("last_name") var lastName: String? = null,
  @SerializedName("email") var email: String? = null,
  @SerializedName("phone") var phone: String? = null,
  @SerializedName("photo") var photo: String? = null,
  @SerializedName("university") var university: String? = null,
  @SerializedName("study_programme") var studyProgramme: String? = null,
  @SerializedName("study_year") var studyYear: Int? = null
): Distinctive, Serializable {
  override fun equals(other: Any?): Boolean {
    return if (other is Distinctive) {
      this.id == other.id
    } else false
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + (facebookId?.hashCode() ?: 0)
    result = 31 * result + (googleId?.hashCode() ?: 0)
    result = 31 * result + (phoneId?.hashCode() ?: 0)
    result = 31 * result + (firstName?.hashCode() ?: 0)
    result = 31 * result + (lastName?.hashCode() ?: 0)
    result = 31 * result + (email?.hashCode() ?: 0)
    result = 31 * result + (phone?.hashCode() ?: 0)
    result = 31 * result + (photo?.hashCode() ?: 0)
    result = 31 * result + (university?.hashCode() ?: 0)
    result = 31 * result + (studyProgramme?.hashCode() ?: 0)
    result = 31 * result + (studyYear ?: 0)
    return result
  }
}
