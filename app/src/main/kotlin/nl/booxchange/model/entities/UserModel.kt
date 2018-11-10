package nl.booxchange.model.entities

import com.google.firebase.firestore.DocumentSnapshot
import nl.booxchange.model.FirestoreObject

data class UserModel(
    override val id: String,
    val image: String,
    val alias: String,
    val last_name: String,
    val books: Int
): FirestoreObject {

    companion object {
        fun fromFirebaseEntry(entry: Pair<String, Map<String, Any>>): UserModel {
            val (key, value) = entry
            return UserModel(
                    key,
                    value["isImage"] as? String ?: "",
                    value["first_name"] as? String ?: "",
                    value["last_name"] as? String ?: "",
                    (value["books"] as? Long)?.toInt() ?: 0
            )
        }

        fun fromFirestoreEntry(entry: DocumentSnapshot): UserModel {
            val (key, value) = entry.id to (entry.data ?: emptyMap())
            return UserModel(
                    key,
                    value["isImage"] as? String ?: "",
                    value["first_name"] as? String ?: "",
                    value["last_name"] as? String ?: "",
                    (value["books"] as? Long)?.toInt() ?: 0
            )
        }
    }
}
