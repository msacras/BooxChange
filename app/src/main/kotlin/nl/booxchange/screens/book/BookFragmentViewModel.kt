package nl.booxchange.screens.book

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.adapters.NumberPickerBindingAdapter.setValue
import android.net.Uri
import android.provider.MediaStore
import android.support.v7.widget.AppCompatRadioButton
import android.view.View
import android.widget.RadioGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.vcristian.combus.expect
import com.vcristian.combus.post
import nl.booxchange.model.*
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.Constants
import nl.booxchange.utilities.Tools
import org.jetbrains.anko.find
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import org.joda.time.DateTime

class BookFragmentViewModel: BaseViewModel(), BookItemHandler, PhotoItemHandler {
    override val isEditModeEnabled = ObservableBoolean(false)
    val isBookUserOwned = ObservableBoolean(false)
    val userOwnedBooks = ObservableArrayList<BookModel>()

    val bookModel = ObservableField<BookModel>()
    var sourceBookModel = BookModel()
        set(value) {
            field = value
            bookModel.set(value.copy())
        }

    val images = ObservableArrayList<ImageModel>()
    var sourceImages = emptyList<ImageModel>()
        set(value) {
            field = value
            images.addAll(value)
        }

    init {
        expect(BookOpenedEvent::class.java) { event ->
            isEditModeEnabled.set(event.bookId.isBlank())
            if (event.bookId.isBlank()) {
                bindBookModel(BookModel())
            } else {
                event.bookModel?.let(::bindBookModel) ?: fetchBook(event.bookId)
            }
        }
    }

    private fun fetchBook(bookId: String) {
        onLoadingStarted()
        FirebaseDatabase.getInstance().getReference("books").child(bookId).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                databaseError.toException().printStackTrace()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.getValue(BookModel::class.java)?.let(::bindBookModel) ?: onLoadingFailed()
                onLoadingFinished()
            }
        })
    }

    private fun bindBookModel(bookModel: BookModel) {
        sourceBookModel = bookModel

        images.clear()
        FirebaseDatabase.getInstance().getReference("images/books").child(bookModel.id).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                databaseError.toException().printStackTrace()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                sourceImages = dataSnapshot.children.map { ImageModel(it.key, ImageModel.EditablePhotoType.REMOTE, Uri.parse(it.value as? String)) }
            }
        })

        isBookUserOwned.set(bookModel.userId == FirebaseAuth.getInstance().currentUser?.uid)

        this.checkedBook.set(null)

        if (!isBookUserOwned.get()) {
            val viewsReference = FirebaseDatabase.getInstance().getReference("books").child(bookModel.id).child("views")

            viewsReference.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    databaseError.toException().printStackTrace()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        viewsReference.setValue(dataSnapshot.getValue(Int::class.java)?.plus(1))
                    }
                }
            })
        }
    }

    fun toggleEditMode(view: View?) {
        isEditModeEnabled.set(!isEditModeEnabled.get())
        if (isEditModeEnabled.get()) {
            images.add(null)
        } else {
            bindBookModel(sourceBookModel)
            images.remove(null)
        }
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

    override fun onAddPhotoFromCameraClick(view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val temporaryImageUri = Tools.getCacheUri("camera_output.jpeg")

        view.context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).forEach { cameraAppPackage ->
            view.context.grantUriPermission(cameraAppPackage.activityInfo.packageName, temporaryImageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, temporaryImageUri)
        post(StartActivity(intent, Constants.REQUEST_CAMERA, BookActivity::class.java))
    }

    override fun onAddPhotoFromGalleryClick(view: View) {
        post(StartActivity(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), Constants.REQUEST_GALLERY, BookActivity::class.java))
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
        val progressDialog = view.context.indeterminateProgressDialog("Saving")
        val mainImage = images.firstOrNull()

        FirebaseDatabase.getInstance().getReference("books").child(bookModel.get()!!.id).setValue(bookModel.get()!!.toFirebaseEntry()).addOnCompleteListener {
            sourceBookModel = bookModel.get()!!
            progressDialog.dismiss()
        }

        sourceImages.filter { it.type == ImageModel.EditablePhotoType.REMOTE }.forEach { photoModel ->
            if (photoModel !in images) {
                FirebaseDatabase.getInstance().getReference(photoModel.path.path).removeValue()
                FirebaseStorage.getInstance().getReference(photoModel.path.path).delete()
            }
        }

        images.filter { it?.type == ImageModel.EditablePhotoType.LOCAL }.forEach { photoModel ->
            FirebaseStorage.getInstance().getReference("images/books/${bookModel.get()!!.id}/${DateTime.now().millis}").putFile(photoModel.path).addOnCompleteListener {
                FirebaseDatabase.getInstance().getReference("images/books/${bookModel.get()!!.id}").push().setValue(it.result.metadata?.path)
                if (photoModel == mainImage) {
                    FirebaseDatabase.getInstance().getReference("books").child(bookModel.get()!!.id).child("" +
                            "").setValue(it.result.metadata?.path)
                }
            }
        }
    }

    fun deleteBook() {
        FirebaseDatabase.getInstance().getReference("books").child(bookModel.get()!!.id).removeValue()
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

        FirebaseFunctions.getInstance("europe-west1").getHttpsCallable("requestBookTrade").call(requestData).addOnCompleteListener {
            view.context.toast(if (it.isSuccessful) "Request sent!" else "Failed to send your request")
        }

/*
        Chat.sendRequest(id.single() ?: "", tradeChoice.single()?.name ?: "", checkedBook.single()?.id ?: "") {
            it?.message?.let(view.context::toast) ?: view.context.toast("requesting failed")
        }
*/
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val imageUri = when (requestCode) {
                Constants.REQUEST_CAMERA -> Tools.getCacheUri("camera_output.jpeg")
                Constants.REQUEST_GALLERY -> data!!.data
                else -> return
            }

            if (images.find { it?.path == imageUri } == null) {
                images.add(images.indexOf(null).coerceAtLeast(0), ImageModel(null, ImageModel.EditablePhotoType.LOCAL, imageUri))
            }
        }
    }
}
