package nl.booxchange.model

import androidx.databinding.ObservableBoolean
import android.view.View
import nl.booxchange.model.entities.ImageModel

interface PhotoItemHandler {
    val isEditModeEnabled: ObservableBoolean

    fun onRemovePhotoClick(photoModel: ImageModel)
    fun View.onAddPhotoFromCameraClick()
    fun View.onAddPhotoFromGalleryClick()

    fun setMainPhoto(photoModel: ImageModel)
    fun isMainPhoto(photoModel: ImageModel): Boolean
}
