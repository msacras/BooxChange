package nl.booxchange.utilities

import android.databinding.BindingAdapter
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.LayoutRes
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TextInputLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.bumptech.glide.*
import com.bumptech.glide.load.resource.bitmap.*
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import nl.booxchange.R
import nl.booxchange.extension.*
import nl.booxchange.model.Distinctive
import nl.booxchange.model.EditablePhotoModel
import org.jetbrains.anko.dip
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import android.view.ViewGroup.MarginLayoutParams



@BindingAdapter("adapterLayout", "adapterHandler", requireAll = false)
fun RecyclerView.setupSimpleRecyclerAdapter(@LayoutRes withLayoutResourceId: Int, withHandler: Any?) {
    adapter = BindingRecyclerAdapter(withLayoutResourceId, withHandler)
}

@BindingAdapter("adapterItems")
fun RecyclerView.updateRecyclerAdapterItems(itemsList: Collection<Distinctive>?) {
    (adapter as? BindingRecyclerAdapter)?.swapItems(itemsList ?: emptyList())
}

@BindingAdapter("photoItems", "photoHandler", requireAll = false)
fun ViewPager.updatePhotoViewPagerItems(itemsList: List<EditablePhotoModel?>?, handler: Any?) {
    adapter = (adapter as? PhotosPagerAdapter ?: PhotosPagerAdapter(handler)).apply { swapItems(itemsList ?: emptyList()) }
}

@BindingAdapter("scaledImageId")
fun AppCompatImageView.setScaledImageFromUrl(imageId: String?) {
    val requestOptions = RequestOptions().error(context.getDrawableCompat(R.drawable.ic_no_image).setTintCompat(R.color.midGray)).transforms(CenterCrop(), RoundedCorners(dip(4)))
    Glide.with(context).load(imageId?.staticResourceUrl).apply(requestOptions).transition(DrawableTransitionOptions().crossFade()).into(this)
}

@BindingAdapter("scaledImageId")
fun AppCompatImageView.setScaledImageFromUrl(photoModel: EditablePhotoModel?) {
    val path = when (photoModel?.type) {
        EditablePhotoModel.EditablePhotoType.LOCAL_URI -> photoModel.path
        EditablePhotoModel.EditablePhotoType.REMOTE_URL -> photoModel.path.path.staticResourceUrl
        else -> null
    }

    val requestOptions = RequestOptions().error(context.getDrawableCompat(R.drawable.ic_no_image).setTintCompat(R.color.midGray)).transforms(CenterCrop(), RoundedCorners(dip(4)))
    Glide.with(context).load(path).apply(requestOptions).transition(DrawableTransitionOptions().crossFade()).into(this)
}

@BindingAdapter("sourceImageId")
fun AppCompatImageView.setFullSizeImageFromUrl(photoModel: EditablePhotoModel?) {
    photoModel ?: return

    doAsync {
        val path = when (photoModel.type) {
            EditablePhotoModel.EditablePhotoType.LOCAL_URI -> photoModel.path
            EditablePhotoModel.EditablePhotoType.REMOTE_URL -> photoModel.path.path.staticResourceUrl
        }

        val drawable = Glide.with(context).load(path).submit(1024, 1024).get()
        uiThread { setImageDrawable(drawable) }
    }
}

@BindingAdapter("compositeImageIds")
fun AppCompatImageView.setImageFromUrl(imageIds: List<String?>?) {
    val noImageDrawable = context.getDrawableCompat(R.drawable.account)
    val imageDrawables = MutableList<Drawable?>(imageIds?.size ?: 0) { null }

    fun updateState() {
        if (imageDrawables.none { it == null }) {
            val imageTransformOptions = RequestOptions.circleCropTransform()
            Glide.with(context).load(stitchDrawables(imageDrawables.toList().filterNotNull(), width, height)).apply(imageTransformOptions).into(this)
        }
    }

    imageIds?.let {
        imageIds.forEachIndexed { index, imageId ->
            doAsync {
                imageDrawables[index] = imageId?.staticResourceUrl?.let(Glide.with(context)::load)?.submit(width, height)?.get() ?: noImageDrawable
                post(::updateState)
            }
        }
    }
}

fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val offsetX = drawable.bounds.run { ((right - left) - width) / 2f }
    val offsetY = 0f

    canvas.translate(-offsetX, -offsetY)
    drawable.draw(canvas)
    canvas.translate(offsetX, offsetY)

    return bitmap
}

fun stitchDrawables(drawables: List<Drawable>, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    val columns = if (drawables.size > 1) 2 else 1
    val rows = if (drawables.size > 2) 2 else 1

    paint.strokeWidth = Tools.safeContext.dip(2).toFloat()
    paint.color = Color.WHITE

    drawables.forEach { it.setBounds(0, 0, width, height) }

    drawables.getOrNull(0)?.let { canvas.drawBitmap(drawableToBitmap(it, width / columns, height), 0f, 0f, paint) }
    drawables.getOrNull(1)?.let { canvas.drawBitmap(drawableToBitmap(it, width / 2, height / rows), width / 2f, 0f, paint) }
    drawables.getOrNull(2)?.let { canvas.drawBitmap(drawableToBitmap(it, width / 2, height / 2), width / 2f, height / 2f, paint) }

    if (drawables.size > 1) canvas.drawLine(width / 2f, 0f, width / 2f, height.toFloat(), paint)
    if (drawables.size > 2) canvas.drawLine(width / 2f, height / 2f, width.toFloat(), height / 2f, paint)

    return bitmap
}

@BindingAdapter("visibleIf")
fun View.setViewVisibility(isVisible: Boolean) {
    setVisible(isVisible)
}

@BindingAdapter("android:stateListAnimator")
fun View.setStateListAnimator(any: Any?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        stateListAnimator = AppCompatButton(context).stateListAnimator.clone()
    }
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

@BindingAdapter("app:layout_constraintWidth_max")
fun View.setMaxConstraintWidth(dpWidth: Int) {
    layoutParams = (layoutParams as? ConstraintLayout.LayoutParams)?.apply {
        matchConstraintMaxWidth = dip(dpWidth)
    }
}

@BindingAdapter("android:layout_marginStart")
fun View.setStartMargin(dimension: Float) {
    layoutParams = (layoutParams as MarginLayoutParams).apply {
        marginStart = dimension.toInt()
    }
}

@BindingAdapter("android:layout_marginEnd")
fun View.setEndMargin(dimension: Float) {
    layoutParams = (layoutParams as MarginLayoutParams).apply {
        marginEnd = dimension.toInt()
    }
}

/*
@BindingAdapter("android:text")
fun AppCompatEditText.setSpannableText(string: SpannableString?) {
    setText(string)
}

@InverseBindingAdapter(attribute = "android:text")
fun AppCompatEditText.getSpannableText(): SpannableString {
    return SpannableString(text)
}
*/
