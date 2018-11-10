package nl.booxchange.model.entities

import android.net.Uri
import java.io.Serializable

class ImageModel(var type: EditablePhotoType, var path: Uri): Serializable {

    companion object {
        val addingItem = ImageModel(EditablePhotoType.ADD, Uri.EMPTY)
    }

    enum class EditablePhotoType {
        LOCAL, REMOTE, ADD, EDIT
    }
}
