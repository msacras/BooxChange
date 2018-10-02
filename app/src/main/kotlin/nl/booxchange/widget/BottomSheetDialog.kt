package nl.booxchange.widget

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.dialog.*
import nl.booxchange.R
import nl.booxchange.screens.settings.SettingsActivity
import nl.booxchange.utilities.Constants
import java.io.ByteArrayOutputStream

class BottomSheetDialog : BottomSheetDialogFragment() {

    private var filePath:Uri?=null

    internal var storage:FirebaseStorage?=null
    internal var storageRef:StorageReference?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storage = FirebaseStorage.getInstance()
        storageRef = storage?.reference

        take_photo_button.setOnClickListener {
            val intent = Intent()
//            val outputUri = Tools.getCacheUri("camera_output")
            intent.action = android.provider.MediaStore.ACTION_IMAGE_CAPTURE
//            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, outputUri)
            startActivityForResult(intent, Constants.REQUEST_CAMERA)
        }

        upload_photo_button.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.REQUEST_GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
            val inputStream = activity?.contentResolver?.openInputStream(data?.data)
            ByteArrayOutputStream().apply {
                inputStream?.copyTo(this)
            }
//            (activitty as SettingsActivity).profile_image?.setImageBitmap(inputStream)
        }

        val sheet = dialog
        if (requestCode == Constants.REQUEST_GALLERY) {
            if (data != null) {
                filePath = data.data
                val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, filePath)
                Glide.with(activity as SettingsActivity).load(bitmap).apply(RequestOptions().circleCrop()).into((activity as SettingsActivity).profile_image)
                if ((activity as SettingsActivity).profile_image?.drawable == null) {
                    val userUid = FirebaseAuth.getInstance().currentUser?.uid
                    val imageRef = FirebaseStorage.getInstance().getReference("images/userphoto/" + userUid)
                    imageRef?.putFile(filePath!!)?.addOnSuccessListener {
                        Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                    }
                    sheet.dismiss()
                }
            }
        }
    }
}
