package nl.booxchange.model.entities

import android.net.Uri
import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import nl.booxchange.model.FirebaseObject
import java.io.Serializable

class ImageModel(id: String?): Serializable, FirebaseObject {
    @Exclude
    override var id = id ?: FirebaseDatabase.getInstance().getReference("images/books").push().key!!

    constructor(id: String?, type: EditablePhotoType, path: Uri): this(id) {
        this.type = type
        this.path = path
    }

    var type: EditablePhotoType = EditablePhotoType.ADD
    var path: Uri = Uri.EMPTY

    companion object {
        val addingItem = ImageModel(null, EditablePhotoType.ADD, Uri.EMPTY)
    }

    enum class EditablePhotoType {
        LOCAL, REMOTE, ADD, EDIT
    }
}
