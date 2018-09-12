package nl.booxchange.model

import android.databinding.ObservableBoolean
import android.view.View
import nl.booxchange.model.entities.ImageModel

interface PhotoItemHandler {
    val isEditModeEnabled: ObservableBoolean

    fun onRemovePhotoClick(photoModel: ImageModel)
    fun onAddPhotoFromCameraClick(view: View)
    fun onAddPhotoFromGalleryClick(view: View)

    fun setMainPhoto(photoModel: ImageModel)
    fun isMainPhoto(photoModel: ImageModel): Boolean
}
