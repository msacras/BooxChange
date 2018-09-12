package nl.booxchange.model.entities

import nl.booxchange.model.FirebaseObject

data class UserModel(
        override val id: String,
        val image: String,
        val alias: String,
        val last_name: String,
        val books: Int
) : FirebaseObject {
/*    fun getFormattedName(): String {
        return alias.split("‼").joinToString(" ")
    }*/

    companion object {
        fun fromFirebaseEntry(entry: Pair<String, Map<String, Any>>): UserModel {
            val (key, value) = entry
            return UserModel(
                    key,
                    value["image"] as? String ?: "",
                    value["alias"] as? String ?: "",
                    value["last_name"] as? String ?: "",
                    (value["books"] as? Long)?.toInt() ?: 0
            )
        }
    }
}
