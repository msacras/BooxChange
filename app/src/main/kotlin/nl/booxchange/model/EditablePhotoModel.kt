package nl.booxchange.model

import android.net.Uri

data class EditablePhotoModel(
    val type: EditablePhotoType,
    val path: Uri
) {
    enum class EditablePhotoType {
        LOCAL_URI, REMOTE_URL
    }
}
