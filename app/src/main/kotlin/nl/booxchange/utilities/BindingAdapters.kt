package nl.booxchange.utilities

import android.arch.lifecycle.LiveData
import android.databinding.BindingAdapter
import android.net.Uri
import android.support.design.widget.TextInputLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
import nl.booxchange.extension.setVisible
import nl.booxchange.extension.takeNotBlank
import nl.booxchange.model.ImageModel
import org.jetbrains.anko.dip
import org.jetbrains.anko.displayMetrics


@BindingAdapter("recyclerLayout", "recyclerHandler", "recyclerItems")
fun <T: Any> RecyclerView.setupLiveRecyclerAdapter(viewConfigs: List<ViewHolderConfig<T>>?, withHandler: Any?, itemsList: LiveData<List<T>>?) {
    if (adapter != null || itemsList == null || viewConfigs == null) return

    adapter = LiveBindingListAdapter(viewConfigs, withHandler).apply { observeList(itemsList) }
}

@BindingAdapter("photoHandler")
fun ViewPager.setupPhotoViewPagerItems(handler: Any?) {
    adapter = PhotosPagerAdapter(handler)
}

@BindingAdapter("photoItems")
fun ViewPager.updatePhotoViewPagerItems(itemsList: List<ImageModel?>?) {
    (adapter as? PhotosPagerAdapter)?.swapItems(itemsList ?: emptyList())
}

@BindingAdapter("scaledImageId")
fun AppCompatImageView.setScaledImageFromUrl(imageId: String?) {
    val emptyDrawable = context.getDrawableCompat(R.drawable.ic_no_image).setTintCompat(R.color.midGray)
    val requestOptions = RequestOptions()
        .error(emptyDrawable)
        .placeholder(emptyDrawable)
        .transforms(CenterCrop(), RoundedCorners(dip(4)))

    Glide.with(context)
        .load(imageId?.takeNotBlank?.let(FirebaseStorage.getInstance()::getReference))
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

    when (imageModel?.type) {
        ImageModel.EditablePhotoType.LOCAL -> Glide.with(context).load(imageModel.path).apply(requestOptions).transition(DrawableTransitionOptions().crossFade()).into(this)
        ImageModel.EditablePhotoType.REMOTE -> Glide.with(context)
            .load(storagePath?.path?.let(FirebaseStorage.getInstance()::getReference))
            .apply(requestOptions)
            .transition(DrawableTransitionOptions().crossFade())
            .into(this)
    }
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
            .load(storagePath?.path?.let(FirebaseStorage.getInstance()::getReference))
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

@BindingAdapter("visibleIf")
fun View.setViewVisibility(isVisible: Boolean) {
    setVisible(isVisible)
}

@BindingAdapter("error")
fun TextInputLayout.setErrorText(errorText: String?) {
    error = errorText?.takeNotBlank
    isErrorEnabled = error != null
}

@BindingAdapter("textFormat")
fun AppCompatEditText.setTextFormatting(template: String?) {
    template ?: return

    addTextChangedListener(object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            val input = s.toString().replace("[^0-9]+".toRegex(), "")

            var charIndex = 0
            val formatted = template.toMutableList().let { template ->
                template.indices.forEach { i ->
                    if (template[i] == '#' && charIndex < input.length) {
                        template[i] = input[charIndex++]
                    }
                }
                template.joinToString("").trim('-', '#')
            }

            if (s.toString() != formatted) {
                val cursorPosition = if (selectionStart == s.toString().length) formatted.length else selectionStart
                setText(formatted)
                setSelection(cursorPosition.coerceAtMost(formatted.length))
            }
        }
    })
}
