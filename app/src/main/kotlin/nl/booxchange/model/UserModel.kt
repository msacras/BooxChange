package nl.booxchange.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import nl.booxchange.extension.hashCode
import nl.booxchange.extension.takeNotBlank
import java.io.Serializable

@Entity(tableName = "users")
data class UserModel(

    @PrimaryKey
    @SerializedName("user_id")
    @ColumnInfo(name = "user_id")
    override val id: String,

    @SerializedName("first_name")
    @ColumnInfo(name = "first_name")
    var firstName: String? = null,

    @SerializedName("last_name")
    @ColumnInfo(name = "last_name")
    var lastName: String? = null,

    @SerializedName("email")
    @ColumnInfo(name = "email")
    var email: String? = null,

    @SerializedName("phone")
    @ColumnInfo(name = "phone")
    var phoneId: String? = null,

    @SerializedName("facebook")
    @ColumnInfo(name = "facebook")
    var facebookId: String? = null,

    @SerializedName("google")
    @ColumnInfo(name = "google")
    var googleId: String? = null,

    @SerializedName("photo")
    @ColumnInfo(name = "photo")
    var photo: String? = null,

    @SerializedName("university")
    @ColumnInfo(name = "university")
    var university: String? = null,

    @SerializedName("study_programme")
    @ColumnInfo(name = "study_programme")
    var studyProgramme: String? = null,

    @SerializedName("study_year")
    @ColumnInfo(name = "study_year")
    var studyYear: Int? = null

): Distinctive, Serializable {
/*
    override fun equals(other: Any?) = (other as? Distinctive)?.id == id
    override fun hashCode() = hashCode
*/

    fun getFormattedName(): String {
        return "${firstName ?: ""} ${lastName ?: ""}".takeNotBlank ?: "Anonymous"
    }
}
