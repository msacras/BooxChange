package nl.booxchange.model

import android.databinding.ObservableBoolean
import android.view.View

interface PhotoItemHandler {
    val isEditModeEnabled: ObservableBoolean

    fun onRemovePhotoClick(photoModel: EditablePhotoModel)
    fun onAddPhotoFromCameraClick(view: View)
    fun onAddPhotoFromGalleryClick(view: View)

    fun setMainPhoto(photoModel: EditablePhotoModel)
    fun isMainPhoto(photoModel: EditablePhotoModel): Boolean
}
