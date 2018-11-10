package nl.booxchange.utilities.databinding

import androidx.databinding.BindingAdapter
import android.net.Uri
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import nl.booxchange.R
import nl.booxchange.extension.getDrawableCompat
import nl.booxchange.extension.setTintCompat
import nl.booxchange.extension.takeNotBlank
import nl.booxchange.model.entities.ImageModel
import org.jetbrains.anko.dip
import org.jetbrains.anko.displayMetrics

@BindingAdapter("scaledImageId")
fun AppCompatImageView.setScaledImageFromUrl(imageId: String?) {
    val emptyDrawable = context.getDrawableCompat(R.drawable.ic_no_image).setTintCompat(R.color.midGray)
    val requestOptions = RequestOptions()
        .error(emptyDrawable)
        .placeholder(emptyDrawable)
        .transforms(CenterCrop(), RoundedCorners(dip(4)))

    Glide.with(context)
        .load(imageId)
        .apply(requestOptions)
        .transition(DrawableTransitionOptions().crossFade())
        .into(this)
}

@BindingAdapter("scaledImageId")
fun AppCompatImageView.setScaledImageFromUrl(imageModel: ImageModel?) {
    val storagePath = imageModel?.path?.takeIf { imageModel.path != Uri.EMPTY }
    val emptyDrawable = context.getDrawableCompat(R.drawable.ic_no_image).setTintCompat(R.color.midGray)
    val requestOptions = RequestOptions()
        .error(emptyDrawable)
        .placeholder(emptyDrawable)
        .transforms(CenterCrop(), RoundedCorners(dip(4)))

//    when (imageModel?.type) {
//        ImageModel.EditablePhotoType.LOCAL -> Glide.with(context)
//            .load(imageModel.path)
//            .apply(requestOptions)
//            .transition(DrawableTransitionOptions().crossFade())
//            .into(this)
        /*ImageModel.EditablePhotoType.REMOTE -> */Glide.with(context)
            .load(storagePath)
            .apply(requestOptions)
            .transition(DrawableTransitionOptions().crossFade())
            .into(this)
//    }
}

@BindingAdapter("sourceImageId")
fun AppCompatImageView.setFullSizeImageFromUrl(imageModel: ImageModel?) {
    val storagePath = imageModel?.path?.takeIf { imageModel.path != Uri.EMPTY }
    val widestEdge = context.displayMetrics.run { Math.max(heightPixels, widthPixels) }
    val emptyDrawable = context.getDrawableCompat(R.drawable.ic_no_image).setTintCompat(R.color.midGray)
    val requestOptions = RequestOptions()
        .error(emptyDrawable)
        .placeholder(emptyDrawable)
        .override(widestEdge, widestEdge)

    when (imageModel?.type) {
        ImageModel.EditablePhotoType.LOCAL -> Glide.with(context).load(imageModel.path).apply(requestOptions).transition(DrawableTransitionOptions().crossFade()).into(this)
        ImageModel.EditablePhotoType.REMOTE -> Glide.with(context)
            .load(storagePath)
            .apply(requestOptions)
            .transition(DrawableTransitionOptions().crossFade())
            .into(this)
    }
}

@BindingAdapter("roundedImageId")
fun AppCompatImageView.setUserImageFromStorageResource(imageId: String?) {
    val emptyDrawable = context.getDrawableCompat(R.drawable.account_circle).setTintCompat(R.color.midGray)
    val requestOptions = RequestOptions()
        .error(emptyDrawable)
        .placeholder(emptyDrawable)
        .transforms(CenterCrop(), CircleCrop())

    @Suppress("IMPLICIT_CAST_TO_ANY")
    val imageReference = if (imageId?.startsWith("http") == true) imageId else imageId?.takeNotBlank?.let(FirebaseStorage.getInstance()::getReference)

    Glide.with(context)
        .load(imageReference)
        .apply(requestOptions)
        .transition(DrawableTransitionOptions().crossFade())
        .into(this)
}
