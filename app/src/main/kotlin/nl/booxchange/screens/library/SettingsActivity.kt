package nl.booxchange.screens.library

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.provider.MediaStore
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatCheckedTextView
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.vcristian.combus.dismiss
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.dialog_image.*
import kotlinx.android.synthetic.main.dialog_name.*
import kotlinx.android.synthetic.main.dialog_phone.*
import nl.booxchange.BuildConfig
import nl.booxchange.R
import nl.booxchange.R.id.*
import nl.booxchange.extension.string
import nl.booxchange.extension.withExitSymbol
import nl.booxchange.screens.SignInActivity
import nl.booxchange.utilities.Constants
import nl.booxchange.utilities.Tools
import org.jetbrains.anko.dip
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity(), OnCompleteListener<AuthResult> {

    private val userUid = FirebaseAuth.getInstance().currentUser?.uid!!.string
    private val dbRef = FirebaseDatabase.getInstance().reference.child("users/").child(userUid)
    private val facebookCallbackManager = CallbackManager.Factory.create()
    private val providers = FirebaseAuth.getInstance().currentUser?.providerData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        var googleId = providers?.find { it.providerId == "google.com" }?.uid
        var facebookId = providers?.find { it.providerId == "facebook.com" }?.uid


        facebook_checkbox.isChecked = providers?.any { it.providerId == "facebook.com" } ?: false
        google_checkbox.isChecked = providers?.any { it.providerId == "google.com" } ?: false
        phone_checkbox.isChecked = providers?.any { it.providerId == "phone" } ?: false

        initializeFacebookAuthorization(facebookId != null)
        initializeGoogleAuthorization(googleId != null)
        initializePhoneAuthorizationLayout((providers?.find { it.providerId == "phone" }?.phoneNumber) != null)

        updateSwitchesAvailability()

        version.text = ("v." + BuildConfig.VERSION_NAME)

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                databaseError.toException().printStackTrace()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val firstName = dataSnapshot.child("first_name").value.toString()
                val lastName = dataSnapshot.child("last_name").value.toString()
                val userPhoto = dataSnapshot.child("imageUrl").value.toString()
                Glide.with(applicationContext).load(userPhoto).apply(RequestOptions().circleCrop()).into(profile_image)
                user_name.text = Editable.Factory.getInstance().newEditable("$firstName $lastName")
            }
        })

        profile_image.setOnClickListener {
            dialogImage()
        }
        user_name.setOnClickListener {
            dialogName()
        }
        back.setOnClickListener { onBackPressed() }
        listOf(user_name).forEach {
            it.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (s.toString().trim().isEmpty()) {
                        it.error = "!"
                    } else {
                        it.error = null
                        back.setOnClickListener {
                            onBackPressed()
                        }
                    }
                }
            })
        }
        sign_out.setOnClickListener {
            logOut()
        }
        listOf(sound_checkbox).forEach(::buildBookCheckbox)
    }

    private fun getUserCountryCode(): String {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

        return (Constants.IDDC[telephonyManager?.simCountryIso?.toUpperCase()]
                ?: Constants.IDDC["NL"]!!).withExitSymbol
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildBookCheckbox(checkbox: AppCompatCheckedTextView) = with(checkbox) {
        val bookIconSize = 32 //dp

        val openingAnimatedDrawable = DrawableCompat.wrap(AnimatedVectorDrawableCompat.create(checkbox.context, R.drawable.animation_book_opening)!!)
        val closingAnimatedDrawable = DrawableCompat.wrap(AnimatedVectorDrawableCompat.create(checkbox.context, R.drawable.animation_book_closing)!!)

        openingAnimatedDrawable.setBounds(0, 0, dip(bookIconSize), dip(bookIconSize))
        closingAnimatedDrawable.setBounds(0, 0, dip(bookIconSize), dip(bookIconSize))

        checkMarkDrawable = if (isChecked) openingAnimatedDrawable else closingAnimatedDrawable

        checkbox.setOnClickListener {
            isChecked = !isChecked
            checkMarkDrawable = if (isChecked) openingAnimatedDrawable else closingAnimatedDrawable
            (checkMarkDrawable as AnimatedVectorDrawableCompat).start()

            (tag as? (Boolean) -> Unit)?.invoke(isChecked)
        }
    }

    private fun AppCompatCheckedTextView.setOnCheckedChangedListener(checkedChangedCallback: (Boolean) -> Unit) {
        tag = checkedChangedCallback
    }

    private fun initializeGoogleAuthorization(isLoggedIn: Boolean) {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.google_web_client_id)).requestEmail().build()
        val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        google_checkbox.isChecked = isLoggedIn

        google_checkbox.setOnCheckedChangeListener { _, checked ->
            val progressDialog = indeterminateProgressDialog("Wait")
            if (checked) {
                startActivityForResult(googleSignInClient.signInIntent, 9001)
                progressDialog.dismiss()
            } else {
                FirebaseAuth.getInstance().currentUser?.unlink("google.com")?.addOnSuccessListener {
                    updateSwitchesAvailability()
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun initializeFacebookAuthorization(isLoggedIn: Boolean) {
        facebook_checkbox.isChecked = isLoggedIn

        facebook_checkbox.setOnCheckedChangeListener { _, checked ->
            val progressDialog = indeterminateProgressDialog("Wait")
            if (checked) {
                LoginManager.getInstance().logInWithReadPermissions(this, emptyList())
                progressDialog.dismiss()
//                facebook_checkbox.trackDrawable.setColorFilter(ContextCompat.getColor(this, R.color.com_facebook_blue), PorterDuff.Mode.SRC_IN)
            } else {
                FirebaseAuth.getInstance().currentUser?.unlink("facebook.com")?.addOnSuccessListener {
                    updateSwitchesAvailability()
                    progressDialog.dismiss()
                }
                LoginManager.getInstance().logOut()
                FirebaseAuth.getInstance().signOut()
            }
        }
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        toast("succeeded Facebook auth")
                        FirebaseAuth.getInstance().currentUser?.linkWithCredential(FacebookAuthProvider.getCredential(loginResult.accessToken.token))?.addOnCompleteListener(this@SettingsActivity)
                    }

                    override fun onCancel() {
                        toast("canceled Facebook auth")
                        //TODO: Handle canceled Facebook auth!
                    }

                    override fun onError(exception: FacebookException) {
                        toast("failed Facebook auth")
                        exception.printStackTrace()
                        //TODO: Handle failed Facebook auth!
                    }
                })
    }

    private fun initializePhoneAuthorizationLayout(isLoggedIn: Boolean) {
        phone_checkbox.isChecked = isLoggedIn
        val dialog = Dialog(this, R.style.BottomSheetDialogTheme)
        dialog.setContentView(R.layout.dialog_phone)
        dialog.window.setGravity(Gravity.BOTTOM)
        dialog.country_code_field.setText(getUserCountryCode())
        phone_checkbox.setOnCheckedChangeListener { _, checked ->
            val progressDialog = indeterminateProgressDialog("Wait")
            if (checked) {
                dialog.show()
                dialog.save_phone.setOnClickListener {
                    val phoneNumber = ("${dialog.country_code_field.text} ${dialog.change_phone.text}").takeIf { it.isNotBlank() }
                            ?: return@setOnClickListener

                    (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(dialog.change_phone.windowToken, 0)

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                            toast("succeeded Phone auth")
                            FirebaseAuth.getInstance().currentUser?.linkWithCredential(phoneAuthCredential)?.addOnCompleteListener(this@SettingsActivity)
                        }

                        override fun onVerificationFailed(firebaseException: FirebaseException?) {
                            toast("failed Phone auth")
                            firebaseException?.printStackTrace()
                            //TODO: Handle failed Phone auth!
                        }
                    })
                    dialog.dismiss()
                }
            } else {
                FirebaseAuth.getInstance().currentUser?.unlink("phone")?.addOnSuccessListener {
                    updateSwitchesAvailability()
                    progressDialog.dismiss()
                }
            }
        }
    }

    override fun onBackPressed() {
        val userName = user_name.text.toString()
        if (userName.trim() == "") {
            toast("Name cannot be empty")
        } else {
            this.finish()
        }
    }

    private fun logOut() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finishAffinity()
        LoginManager.getInstance().logOut()
        FirebaseAuth.getInstance().signOut()
    }

    private fun dialogImage() {
        val dialog = Dialog(this, R.style.BottomSheetDialogTheme)
        dialog.setContentView(R.layout.dialog_image)
        dialog.window.setGravity(Gravity.BOTTOM)
        dialog.camera_photo_button.setOnClickListener {
            onAddPhotoFromCameraClick()
            dialog.dismiss()
        }
        dialog.gallery_photo_button.setOnClickListener {
            onAddPhotoFromGalleryClick()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun dialogName() {
        val dialogName = Dialog(this, R.style.BottomSheetDialogTheme)
        dialogName.setContentView(R.layout.dialog_name)
        dialogName.window.setGravity(Gravity.BOTTOM)
        dialogName.show()
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val firstName = dataSnapshot.child("first_name").value?.string
                val lastName = dataSnapshot.child("last_name").value?.string
                dialogName.change_first_name.setText(firstName)
                dialogName.change_last_name.setText(lastName)
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
        dialogName.upload_name.setOnClickListener {
                if (dialogName.change_first_name.text.toString().trim() == "" || dialogName.change_last_name.text.toString().trim() == "") {
                    toast("Complete all labels")
                } else {
                    val firstNameLabel = dialogName.change_first_name.text.toString()
                    val lastNameLabel = dialogName.change_last_name.text.toString()
                    val uploadFirstName = dbRef.child("first_name")
                    val uploadLastName = dbRef.child("last_name")
                    uploadLastName.setValue(lastNameLabel)
                    uploadFirstName.setValue(firstNameLabel, { databaseError, _ ->
                        if (databaseError != null) {
                            toast("Data could not be saved. " + databaseError.message)
                        } else {
                            toast("Success saved")
                            dialogName.dismiss()
                        }
                    })
                    (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(dialogName.change_first_name.windowToken, 0)
                    dialogName.dismiss()
            }
        }

    }

    private fun onAddPhotoFromGalleryClick() {
        startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), Constants.REQUEST_GALLERY)
    }

    private fun onAddPhotoFromCameraClick() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val temporaryImageUri = Tools.getCacheUri("camera_output.jpeg")

        packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).forEach { cameraAppPackage ->
            grantUriPermission(cameraAppPackage.activityInfo.packageName, temporaryImageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, temporaryImageUri)
        startActivityForResult(intent, Constants.REQUEST_CAMERA)
    }

    override fun onComplete(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            val providers = FirebaseAuth.getInstance().currentUser?.providerData
            providers?.find { it.providerId == "phone" }?.phoneNumber
            providers?.find { it.providerId == "facebook.com" }?.uid
            providers?.find { it.providerId == "google.com" }?.uid
        } else {
            toast("Firebase auth task failed")
            //TODO: Handle failed Firebase auth!
        }
        updateSwitchesAvailability()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_GALLERY || requestCode == Constants.REQUEST_CAMERA) {
            val imageUri = when (requestCode) {
                Constants.REQUEST_CAMERA -> Tools.getCacheUri("camera_output.jpeg")
                Constants.REQUEST_GALLERY -> data?.data
                else -> return
            }
            if (imageUri != null) {
                Glide.with(this).load(imageUri).apply(RequestOptions().circleCrop()).into(profile_image)
                val imgPath = FirebaseStorage.getInstance().reference.child("images/userphoto/").child(userUid)
                imgPath.putFile(imageUri).addOnSuccessListener {
                    imgPath.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri
                        dbRef.child("imageUrl").setValue(downloadUrl.string)
                    }
                }
            }
        }
        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                toast("succeeded Google auth")
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().currentUser?.linkWithCredential(credential)?.addOnCompleteListener(this)
            } catch (e: ApiException) {
                toast("failed Google auth")
                e.printStackTrace()
            }
        } else {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun updateSwitchesAvailability() {
        if (FirebaseAuth.getInstance().currentUser?.providerData?.size ?: 0 < 3) {
            facebook_checkbox.isEnabled = !facebook_checkbox.isChecked
            google_checkbox.isEnabled = !google_checkbox.isChecked
            phone_checkbox.isEnabled = !phone_checkbox.isChecked
        } else {
            facebook_checkbox.isEnabled = true
            google_checkbox.isEnabled = true
            phone_checkbox.isEnabled = true
        }
    }
}