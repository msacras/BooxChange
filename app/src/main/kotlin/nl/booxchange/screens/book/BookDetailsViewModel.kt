package nl.booxchange.screens.book

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.app.ActivityCompat
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import nl.booxchange.extension.takeNotBlank
import nl.booxchange.model.CheckableBookItemHandler
import nl.booxchange.model.PhotoItemHandler
import nl.booxchange.model.entities.BookModel
import nl.booxchange.model.entities.BookModel.OfferType
import nl.booxchange.model.entities.ImageModel
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.Constants
import nl.booxchange.utilities.Tools
import nl.booxchange.utilities.database.LiveList
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import org.joda.time.DateTime

class BookDetailsViewModel: BaseViewModel(), CheckableBookItemHandler, PhotoItemHandler {
    override val isEditModeEnabled = ObservableBoolean(false)

    val isBookUserOwned = ObservableBoolean(false)
    val userOwnedBooks = ObservableArrayList<BookModel>()

    val bookModel = ObservableField<BookModel>()
    private var sourceBookModel = BookModel()
        set(value) {
            field = value
            bookModel.set(value.copy())
        }

    val images = ObservableArrayList<ImageModel>()
    private var sourceImages = emptyList<ImageModel>()
        set(value) {
            field = value
            images.clear()
            images.addAll(value)
        }

    val tradeBooks = LiveList<TradeBookModel>()
    private var sourceTradeBooks = emptyList<String>()
        set(value) {
            field = value
            tradeBooks.postValue(value.map(::TradeBookModel))
        }

    fun initializeWithConfig(bookId: String?) {
        isEditModeEnabled.set(bookId == null)
        bookId?.let(::fetchBook) ?: bindBookModel(BookModel())
    }

    private fun fetchBook(bookId: String) {
        onLoadingStarted()
        FirebaseFirestore.getInstance().collection("books").document(bookId).get()
            .addOnSuccessListener {
                bindBookModel(BookModel.fromFirestoreEntry(it))
            }
            .addOnFailureListener {
                it.printStackTrace()
                onLoadingFailed()
            }
            .addOnCompleteListener {
                onLoadingFinished()
            }
    }

    private fun bindBookModel(bookModel: BookModel) {
        sourceBookModel = bookModel

        sourceTradeBooks = bookModel.tradeBooks
        sourceImages = bookModel.images.map { ImageModel(ImageModel.EditablePhotoType.REMOTE, Uri.parse(it)) }
        isBookUserOwned.set(bookModel.userId == FirebaseAuth.getInstance().currentUser?.uid)

        checkedBook.set(null)

        if (!isBookUserOwned.get()) {
            FirebaseFirestore.getInstance().collection("books").document(bookModel.id).update(mapOf("views" to bookModel.views + 1))
        }
    }

    fun toggleEditMode(view: View?) {
        isEditModeEnabled.set(!isEditModeEnabled.get())
        if (isEditModeEnabled.get()) {
            images.add(ImageModel.addingItem)
            tradeBooks += listOf(TradeBookModel(""))
        } else {
            bindBookModel(sourceBookModel)
            images.remove(ImageModel.addingItem)
            tradeBooks.value.find { it?.title?.get() == "" }?.let(tradeBooks::minusAssign)
        }
    }

    fun addBlankTradeBookField() {
        tradeBooks += listOf(TradeBookModel(""))
    }

    fun removeExtraBlankFields(exceptCurrentItem: TradeBookModel) {
        tradeBooks.value.filter { it?.title?.get().isNullOrEmpty() && it != exceptCurrentItem }.filterNotNull().forEach(tradeBooks::minusAssign)
    }

    fun setConditionLevel(radioGroup: RadioGroup, buttonId: Int) {
        bookModel.get()!!.condition.set(radioGroup.find<AppCompatRadioButton>(buttonId).text.toString().toInt())
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

    override fun View.onAddPhotoFromCameraClick() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as AppCompatActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.PERMISSION_STORAGE)
            return
        }

        Tools.generateCameraImageId()

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val temporaryImageUri = Tools.lastCameraImageUri

        context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).forEach { cameraAppPackage ->
            context.grantUriPermission(cameraAppPackage.activityInfo.packageName, temporaryImageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, temporaryImageUri)

        (context as? AppCompatActivity)?.startActivityForResult(intent, Constants.REQUEST_CAMERA)
    }

    override fun View.onAddPhotoFromGalleryClick() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        (context as? AppCompatActivity)?.startActivityForResult(intent, Constants.REQUEST_GALLERY)
    }

    override fun onRemovePhotoClick(photoModel: ImageModel) {
        images.remove(photoModel)
    }

    override fun setMainPhoto(photoModel: ImageModel) {
        images.remove(photoModel)
        images.add(0, photoModel)
    }

    override fun isMainPhoto(photoModel: ImageModel): Boolean {
        if (photoModel.type == ImageModel.EditablePhotoType.REMOTE) {
            photoModel.path
        }
        return images.indexOf(photoModel) == 0
    }

    fun saveBook(view: View) {
        val onImagesUploadFinished = {
            val bookData = bookModel.get()!!.toFirestoreEntry() as MutableMap
            bookData["images"] = images.filter { it.type == ImageModel.EditablePhotoType.REMOTE }.mapIndexed { index, imagePath -> index.toString() to imagePath.path.toString() }.toMap().values.toList()
            bookData["tradeBooks"] = tradeBooks.value.mapNotNull { it?.title?.get()?.takeNotBlank }

            val databaseCall = if (bookModel.get()!!.id.isBlank()) {
                FirebaseFirestore.getInstance().collection("books")::add
            } else {
                FirebaseFirestore.getInstance().collection("books").document(bookModel.get()!!.id)::update
            }

            databaseCall(bookData)
                .addOnSuccessListener {
                    bookModel.get()!!.id = (it as? DocumentReference)?.id ?: bookModel.get()!!.id
                    fetchBook(bookModel.get()!!.id)
                }
                .addOnFailureListener {
                    onLoadingFailed()
                }
                .addOnCompleteListener {
                    onLoadingFinished()
                }
        }

        onLoadingStarted()

        sourceImages.filter { it.type == ImageModel.EditablePhotoType.REMOTE }.forEach { sourcePhotoModel ->
            if (images.find { it.path.path == sourcePhotoModel.path.path } == null) {
                FirebaseStorage.getInstance().getReferenceFromUrl(sourcePhotoModel.path.toString()).delete()
            }
        }

        val imagesToUpload = images.filter { it?.type == ImageModel.EditablePhotoType.LOCAL }
        var imageUploadSemaphore = 0

        if (imageUploadSemaphore == imagesToUpload.size) {
            onImagesUploadFinished()
        }

        imagesToUpload.forEachIndexed { index, photoModel ->
            FirebaseStorage.getInstance().getReference("images/books/${bookModel.get()!!.id}/${DateTime.now().millis}").putFile(photoModel.path)
                .addOnSuccessListener {
                    it.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener { downloadUri ->
                            imagesToUpload[index].apply {
                                type = ImageModel.EditablePhotoType.REMOTE
                                path = downloadUri
                            }

                            if (++imageUploadSemaphore == imagesToUpload.size) {
                                onImagesUploadFinished()
                            }
                        }
                        ?.addOnFailureListener {

                        }
                }
                .addOnFailureListener {
                    //TODO: handle upload failure
                }
        }
    }

    fun deleteBook(view: View) {
        FirebaseFirestore.getInstance().collection("books").document(bookModel.get()!!.id).delete().addOnSuccessListener {
            (view.context as? BookDetailsActivity)?.toast("Book deleted")
        }
    }

    fun sendRequest(view: View) {
        val requestData = mapOf(
            "requestedBookOwnerId" to bookModel.get()?.userId,
            "requestedBookTitle" to bookModel.get()?.title?.get(),
            "requestedBookId" to bookModel.get()?.id,

            "requesterAlias" to FirebaseAuth.getInstance().currentUser?.displayName,
            "requesterId" to FirebaseAuth.getInstance().currentUser?.uid,

            "tradeType" to tradeChoice.get()?.name,
            "tradeBookId" to checkedBook.get()?.id,
            "tradeBookTitle" to checkedBook.get()?.title?.get()
        )

        FirebaseFunctions.getInstance("europe-west1").getHttpsCallable("sendBookTradeRequest").call(requestData).addOnCompleteListener {
            view.context.toast(if (it.isSuccessful) "Request sent!" else "Failed to send your request")
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val imageUri = when (requestCode) {
                Constants.REQUEST_CAMERA -> Tools.lastCameraImageUri
                Constants.REQUEST_GALLERY -> intent!!.data
                else -> return
            }

            if (images.find { it?.path == imageUri } == null) {
                images.add(images.indexOf(ImageModel.addingItem).coerceAtLeast(0), ImageModel(ImageModel.EditablePhotoType.LOCAL, imageUri))
            }
        }
    }
}
