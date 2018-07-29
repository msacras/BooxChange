package nl.booxchange.model

import com.google.gson.annotations.SerializedName
import nl.booxchange.extension.hashCode
import nl.booxchange.extension.takeNotBlank
import java.io.Serializable

data class UserModel(
    @SerializedName("user_id") override val id: String,
    @SerializedName("first_name") var firstName: String? = null,
    @SerializedName("last_name") var lastName: String? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("phone") var phoneId: String? = null,
    @SerializedName("facebook") var facebookId: String? = null,
    @SerializedName("google") var googleId: String? = null,
    @SerializedName("photo") var photo: String? = null,
    @SerializedName("university") var university: String? = null,
    @SerializedName("study_programme") var studyProgramme: String? = null,
    @SerializedName("study_year") var studyYear: Int? = null
): Distinctive, Serializable {
    override fun equals(other: Any?) = (other as? Distinctive)?.id == id
    override fun hashCode() = hashCode

    fun getFormattedName(): String {
        return "${firstName ?: ""} ${lastName ?: ""}".takeNotBlank ?: "Anonymous"
    }
}
