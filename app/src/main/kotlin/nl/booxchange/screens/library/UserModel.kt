package nl.booxchange.screens.library

import nl.booxchange.model.FirebaseObject

data class UserModel(
    override val id: String,
    val image: String,
    val alias: String,
    val books: Int
): FirebaseObject {
    companion object {
        fun fromFirebaseEntry(entry: Pair<String, Map<String, Any>>): UserModel {
            val (key, value) = entry
            return UserModel(
                key,
                value["image"] as? String ?: "",
                value["alias"] as? String ?: "",
                (value["books"] as? Long)?.toInt() ?: 0
            )
        }
    }
}
