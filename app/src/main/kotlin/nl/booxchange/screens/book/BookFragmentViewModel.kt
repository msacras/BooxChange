package nl.booxchange.screens.book

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.net.Uri
import android.provider.MediaStore
import android.support.v7.widget.AppCompatRadioButton
import android.util.Base64
import android.view.View
import android.widget.RadioGroup
import com.vcristian.combus.expect
import com.vcristian.combus.post
import nl.booxchange.api.APIClient.Chat
import nl.booxchange.api.APIClient.Book
import nl.booxchange.extension.takeNotBlank
import nl.booxchange.model.*
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.Constants
import nl.booxchange.utilities.Tools
import nl.booxchange.utilities.UserData
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream

class BookFragmentViewModel: BaseViewModel(), BookItemHandler, PhotoItemHandler {
    override val isEditModeEnabled = ObservableBoolean(false)
    val isBookUserOwned = ObservableBoolean(false)
    val userOwnedBooks = ObservableArrayList<BookModel>()

    private var bookModel: BookModel? = null

    val bookId = ObservableField<String>()
    val title = ObservableField<String>()
    val author = ObservableField<String>()
    val edition = ObservableField<String>()
    val condition = ObservableInt()
    val isbn = ObservableField<String>()
    val info = ObservableField<String>()
    val images = ObservableArrayList<EditablePhotoModel>()
    val offerPrice = ObservableField<String>()
    val isSell = ObservableBoolean()
    val isExchange = ObservableBoolean()

    init {
        expect(BookOpenedEvent::class.java) { event ->
            isEditModeEnabled.set(event.bookModel?.id == "")
            event.bookModel?.let(::bindBookModel) ?: fetchBook(event.bookId)
        }
    }

    private fun fetchBook(bookId: String) {
        onLoadingStarted()
        Book.bookGet(bookId) {
            it?.let(::bindBookModel) ?: onLoadingFailed()
            onLoadingFinished()
        }
    }

    private fun bindBookModel(bookModel: BookModel) {
        bookId.set(bookModel.id)
        title.set(bookModel.title)
        author.set(bookModel.author)
        edition.set(bookModel.edition)
        condition.set(bookModel.condition ?: 0)
        isbn.set(bookModel.isbn)
        info.set(bookModel.info)
        offerPrice.set(bookModel.offerPrice)

        isSell.set(bookModel.offerType?.isSell ?: false)
        isExchange.set(bookModel.offerType?.isExchange ?: false)

        images.clear()
        images.addAll((bookModel.images ?: emptyList()).map { EditablePhotoModel(EditablePhotoModel.EditablePhotoType.REMOTE_URL, Uri.parse(it)) })
        isBookUserOwned.set(bookModel.userId == UserData.Session.userId)

        this.userOwnedBooks.clear()
        this.userOwnedBooks.addAll(UserData.Session.userBooks)
        this.checkedBook.set(null)
        this.bookModel = bookModel
        bookId.get()?.takeIf { it.isNotBlank() && !isBookUserOwned.get() }?.let(Book::incrementViewsCount)
    }

    fun toggleEditMode(view: View) {
        isEditModeEnabled.set(!isEditModeEnabled.get())
        if (isEditModeEnabled.get()) {
            images.add(null)
        } else {
            bindBookModel(bookModel ?: return)
            images.remove(null)
        }
    }

    fun setConditionLevel(radioGroup: RadioGroup, buttonId: Int) {
        condition.set(radioGroup.find<AppCompatRadioButton>(buttonId).text.toString().toInt())
    }

    override val checkedBook = ObservableField<BookModel>()
    val tradeChoice = ObservableField<OfferType>(OfferType.NONE)

    fun setTradeChoice(radioGroup: RadioGroup, buttonId: Int) {
        tradeChoice.set(radioGroup.find<AppCompatRadioButton>(buttonId).tag as OfferType)
    }

    override fun onBookItemClick(view: View, bookModel: BookModel) {
        if (checkedBook.get() == bookModel) {
            checkedBook.set(null)
        } else {
            checkedBook.set(bookModel)
        }
    }

    override fun onAddPhotoFromCameraClick(view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val temporaryImageUri = Tools.getCacheUri("camera_output.jpeg")
        view.context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).forEach { cameraAppPackage ->
            view.context.grantUriPermission(cameraAppPackage.activityInfo.packageName, temporaryImageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, temporaryImageUri)
        post(StartActivity(intent, Constants.REQUEST_CAMERA, BookFragment::class.java))
    }

    override fun onAddPhotoFromGalleryClick(view: View) {
        post(StartActivity(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), Constants.REQUEST_GALLERY, BookFragment::class.java))
    }

    override fun onRemovePhotoClick(photoModel: EditablePhotoModel) {
        images.remove(photoModel)
    }

    override fun setMainPhoto(photoModel: EditablePhotoModel) {
        images.remove(photoModel)
        images.add(0, photoModel)
    }

    override fun isMainPhoto(photoModel: EditablePhotoModel): Boolean {
        return images.indexOf(photoModel) == 0
    }

    fun saveBook(view: View) {
        val imagesData = images.mapNotNull {
            it ?: return@mapNotNull null

            if (it.type == EditablePhotoModel.EditablePhotoType.REMOTE_URL) {
                it.path.path
            } else {
                val inputStream = Tools.safeContext.contentResolver.openInputStream(it.path)
                val byteStream = ByteArrayOutputStream().apply { inputStream.copyTo(this) }
                val base64Image = Base64.encodeToString(byteStream.toByteArray(), Base64.DEFAULT)
                "base64://$base64Image"
            }
        }

        val bookModel = BookModel(
            bookId.get() ?: "",
            title.get(),
            author.get(),
            edition.get(),
            condition.get(),
            isbn.get(),
            info.get(),
            imagesData,
            UserData.Session.userId,
            offerPrice.get(),
            OfferType.getByFilters(isExchange.get(), isSell.get())
        )

        (if (bookModel.id.isEmpty()) Book::bookAdd else Book::bookUpdate).invoke(bookModel) {
            it?.let {
                bindBookModel(it)
                view.context.toast("updating complete")
                //TODO: Update library
            } ?: view.context.toast("updating failed")
        }
    }

    fun deleteBook(view: View) {
        val bookId = bookId.get()?.takeNotBlank ?: run {
            //TODO: Close book view
            return
        }

        Book.bookDelete(bookId) {
            it?.message?.let(view.context::toast) ?: view.context.toast("deleting failed")
        }
    }

    fun postRequest(view: View) {
        Chat.postRequest(bookId.get() ?: "", tradeChoice.get()?.name ?: "", checkedBook.get()?.id ?: "") {
            it?.message?.let(view.context::toast) ?: view.context.toast("requesting failed")
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val imageUri = when (requestCode) {
                Constants.REQUEST_CAMERA -> Tools.getCacheUri("camera_output.jpeg")
                Constants.REQUEST_GALLERY -> data!!.data
                else -> return
            }

            if (images.find { it?.path == imageUri } == null) {
                val photoModel = EditablePhotoModel(EditablePhotoModel.EditablePhotoType.LOCAL_URI, imageUri)
                images.add(images.indexOf(null).coerceAtLeast(0), photoModel)
            }
        }
    }
}
