package nl.booxchange.model

import android.net.Uri
import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import java.io.Serializable

class ImageModel(id: String?): Serializable, FirebaseObject {
    @Exclude
    override var id = id ?: FirebaseDatabase.getInstance().getReference("images/books").push().key!!

    constructor(id: String?, type: EditablePhotoType, path: Uri): this(id) {
        this.type = type
        this.path = path
    }

    var type: EditablePhotoType = EditablePhotoType.NULL
    var path: Uri = Uri.EMPTY

    enum class EditablePhotoType {
        LOCAL, REMOTE, NULL, EDIT
    }
}
