package nl.booxchange.screens

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.BottomSheetDialogFragment
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog.*
import kotlinx.android.synthetic.main.fragment_profile.*
import nl.booxchange.R
import nl.booxchange.model.UserModel
import nl.booxchange.screens.profile.ProfileFragment
import nl.booxchange.utilities.Constants
import nl.booxchange.utilities.Tools
import nl.booxchange.utilities.UserData
import java.io.ByteArrayOutputStream
import java.io.File

class BottomSheetDialog : BottomSheetDialogFragment() {

    //private val userModel: UserModel = UserData.Session.userModel!!.copy()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

/*        take_photo_button.setOnClickListener {
            val intent = Intent()
            val outputUri = Tools.getCacheFile("camera_output")
            intent.action = android.provider.MediaStore.ACTION_IMAGE_CAPTURE
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, outputUri)
            startActivityForResult(intent, Constants.REQUEST_CAMERA)
        }*/
        upload_photo_button.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.REQUEST_GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
/*
        if (requestCode == Constants.REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
            val inputStream = activity?.contentResolver?.openInputStream(data?.data)
            ByteArrayOutputStream().apply {
                inputStream?.copyTo(this)
                userModel.photo = Base64.encodeToString(this.toByteArray(), Base64.DEFAULT)
            }
            (activity as MainFragmentActivity).profile_image?.setImageBitmap(inputStream)
        }
*/
        val sheet = dialog
        if (requestCode == Constants.REQUEST_GALLERY) {
            if (data != null) {
                val contentURI = data.getData()
                val baos = ByteArrayOutputStream()
                val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, contentURI)
                (activity as MainFragmentActivity).profile_image?.setImageBitmap(bitmap)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val b = baos.toByteArray()
                val imageEncode = Base64.encodeToString(b, Base64.DEFAULT)
//                val inputStream = activity!!.contentResolver.openInputStream(data?.data)
                //ByteArrayOutputStream().apply {
                // bitmap.copyTo(this)
                //bitmap = Base64.encodeToString(this.toByteArray(), Base64.DEFAULT)
                UserData.Session.userModel?.photo = imageEncode
                //(activity as MainFragmentActivity).progress_bar?.setText(imageEncode)

                var imageAsBytes = Base64.decode(imageEncode, Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
                (activity as MainFragmentActivity).profile_image?.setImageBitmap(decodedByte)

                if ((activity as MainFragmentActivity).profile_image?.getDrawable() != null) {
                    sheet.dismiss()
                }
            }
        }
    }


/*        if (requestCode == Constants.REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            val inputUri = Tools.getCacheFile("camera_output")
            val inputStream = contentResolver.openInputStream(inputUri)
            ByteArrayOutputStream().apply {
                inputStream.copyTo(this)
                userModel.photo = Base64.encodeToString(this.toByteArray(), Base64.DEFAULT)
            }

        }*/
/*        if (requestCode == Constants.REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
            val inputStream = contentResolver.openInputStream(data?.data)
            ByteArrayOutputStream().apply {
                inputStream.copyTo(this)
                userModel.photo = Base64.encodeToString(this.toByteArray(), Base64.DEFAULT)
            }
        }*/
}
