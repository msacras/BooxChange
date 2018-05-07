package nl.booxchange.extension

import nl.booxchange.model.UserModel

val UserModel.formattedName: String
    get() = "${firstName ?: ""} ${lastName ?: ""}".takeNotBlank ?: "Anonymous"
