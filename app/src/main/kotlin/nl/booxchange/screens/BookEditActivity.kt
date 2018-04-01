package nl.booxchange.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import kotlinx.android.synthetic.main.activity_book_edit.*
import nl.booxchange.R
import nl.booxchange.model.BookModel
import nl.booxchange.utilities.BaseActivity
import nl.booxchange.utilities.Constants
import nl.booxchange.utilities.Tools
import nl.booxchange.utilities.UserData
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream


/**
 * Created by Cristian Velinciuc on 3/14/18.
 */
class BookEditActivity: BaseActivity() {
  private lateinit var bookModel: BookModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_book_edit)
    bookModel = (intent.getSerializableExtra(Constants.EXTRA_PARAM_BOOK_MODEL) as? BookModel)?.copy() ?: BookModel("")
    bookModel.userId = UserData.Session.userModel?.id
    writeFields()
    initializeLayout()
  }

  private fun initializeLayout() {
    back.setOnClickListener { onBackPressed() }
    done.setOnClickListener {
      readFields()
      uploadBook()
    }
    take_photo_button.setOnClickListener {
      val intent = Intent()
      val outputUri = Tools.getCacheFile("camera_output")
      intent.action = android.provider.MediaStore.ACTION_IMAGE_CAPTURE
      intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, outputUri)
      startActivityForResult(intent, Constants.REQUEST_CAMERA)
    }
    upload_photo_button.setOnClickListener {
      val intent = Intent()
      intent.type = "image/*"
      intent.action = Intent.ACTION_GET_CONTENT
      startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.REQUEST_GALLERY)
    }
  }

  private fun writeFields() {
    title_of_the_book_field.setText(bookModel.title ?: "")
    authors_field.setText(bookModel.author ?: "")
    edition_field.setText(bookModel.edition?.toString() ?: "")
    isbn_field.setText(bookModel.isbn ?: "")
    additional_info_field.setText(bookModel.info ?: "")
  }

  private fun readFields() {
    bookModel.title = title_of_the_book_field.text.toString().takeIf { it.isNotBlank() }
    bookModel.author = authors_field.text.toString().takeIf { it.isNotBlank() }
    bookModel.edition = edition_field.text.toString().takeIf { it.isNotBlank() }?.toInt()
    bookModel.isbn = isbn_field.text.toString().takeIf { it.isNotBlank() }
    bookModel.info = additional_info_field.text.toString().takeIf { it.isNotBlank() }
  }

  private fun uploadBook() {
    val requestAction = if (intent.hasExtra(Constants.EXTRA_PARAM_BOOK_MODEL)) requestManager::bookUpdate else requestManager::bookAdd
    loading_view.show()
    loading_view.message = "Uploading"
    requestAction(bookModel) { response ->
      response?.let {
        toast("Upload finished")
        if (response.success) {
          loading_view.message = "Success"
          toast("Request success")
          //TODO: Show success view
          intent.putExtra(Constants.EXTRA_PARAM_BOOK_EDIT_RESULT, true)
          logo.postDelayed({ onBackPressed() }, 1000)
        } else {
          loading_view.hide()
          toast("Request failure")
          //TODO: Show failure view; hide loading view
        }
        null
      } ?: run {
        loading_view.hide()
        toast("Upload failed")
        //TODO: Show connection failure message
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == Constants.REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
      val inputUri = Tools.getCacheFile("camera_output")
      val inputStream = contentResolver.openInputStream(inputUri)
      ByteArrayOutputStream().apply {
        inputStream.copyTo(this)
        bookModel.image = Base64.encodeToString(this.toByteArray(), Base64.DEFAULT)
      }

    }
    if (requestCode == Constants.REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
      val inputStream = contentResolver.openInputStream(data?.data)
      ByteArrayOutputStream().apply {
        inputStream.copyTo(this)
        bookModel.image = Base64.encodeToString(this.toByteArray(), Base64.DEFAULT)
      }
    }
  }

  override fun onBackPressed() {
    if (!intent.hasExtra(Constants.EXTRA_PARAM_BOOK_EDIT_RESULT)) {
      intent.putExtra(Constants.EXTRA_PARAM_BOOK_EDIT_RESULT, false)
    }
    setResult(Constants.REQUEST_BOOK_EDIT, intent)
    super.onBackPressed()
  }
}
