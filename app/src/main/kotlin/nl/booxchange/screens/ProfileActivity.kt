package nl.booxchange.screens

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.GREEN
import android.os.Bundle
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.text.Editable
import android.text.TextWatcher
import android.text.method.TextKeyListener.clear
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieEntry
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.view.*
import nl.booxchange.R
import nl.booxchange.R.id.*
import nl.booxchange.extension.isVisible
import nl.booxchange.extension.setVisible
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
import nl.booxchange.model.UserModel
import nl.booxchange.utilities.BaseActivity
import nl.booxchange.utilities.UserData
import nl.booxchange.utilities.UserData.Session.userModel
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit

class ProfileActivity : BaseActivity(), OnCompleteListener<AuthResult> {

    private val facebookCallbackManager = CallbackManager.Factory.create()
    private val userModel: UserModel = UserData.Session.userModel!!.copy()

    val height = LinearLayout.LayoutParams.WRAP_CONTENT
    val width = LinearLayout.LayoutParams.MATCH_PARENT

    val color = Color.parseColor("#939393")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        writeFields()
        initializeLayout()

        listOf(chevron, fb_icon, not_icon, google_icon, phone_icon, del_l_name, del_f_name, del_email).forEach { it.setColorFilter(color) }
        listOf(f_name to del_f_name, l_name to del_l_name, email to del_email).forEach { (view, delete_view) ->
            view.addTextChangedListener(object : TextWatcher {
                init {
                    delete_view.setOnClickListener {
                        view.text.clear()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (s.toString().trim().isEmpty()) {
                        delete_view.toGone()
                    } else {
                        if (view.isEnabled) {
                            delete_view.toVisible()
                        }
                    }
                }
            })
        }

        Glide.with(applicationContext)
                .load("http://i.imgur.com/DvpvklR.png")
                //.apply(RequestOptions.bitmapTransform(BlurTransformation(10)))
                .apply(RequestOptions.circleCropTransform())
                .into(profile_image)
        back_button.setOnClickListener { onBackPressed() }

    }

    private fun initializeLayout() {

        edit_exit.setOnClickListener {
            email.isEnabled = !email.isEnabled
            listOf(f_name, l_name, university, study_programme, study_year).forEach { it.isEnabled = email.isEnabled }
            listOf(see_more, del_f_name, del_l_name, del_email, save).forEach { it.setVisible(email.isEnabled) }

            if (email.isEnabled) {
                chevron.animate().scaleY(-1f).setDuration(350L).start()
                edit_exit.setImageResource(R.drawable.ic_cancel)
            } else {
                chevron.animate().scaleY(1f).setDuration(300L).setStartDelay(350L).start()
                edit_exit.setImageResource(R.drawable.ic_pencil_black_24dp)
                writeFields()
            }
        }

        save.setOnClickListener {
            chevron.animate().scaleY(1f).setDuration(300L).setStartDelay(350L).start()
            listOf(save, see_more, del_f_name, del_l_name, del_email).forEach { it.toGone() }
            listOf(f_name, l_name, email, university, study_programme, study_year).forEach { it.isEnabled = false }
            edit_exit.setImageResource(R.drawable.ic_pencil_black_24dp)
            readFields()
            uploadUser()
        }

        val providers = FirebaseAuth.getInstance().currentUser?.providerData
        phone_connect.isChecked = providers?.any { it.providerId == "phone" } ?: false
        facebook_connect.isChecked = providers?.any { it.providerId == "facebook.com" } ?: false
        google_connect.isChecked = providers?.any { it.providerId == "google.com" } ?: false

        initializePhoneAuthorizationLayout()
        initializeFacebookAuthorization()
        initializeGoogleAuthorization()
/*
        take_photo_button.setOnClickListener {
            val intent = Intent()
            val outputUri = Tools.getCacheFile("camera_output")
            intent.action = android.provider.MediaStore.ACTION_IMAGE_CAPTURE
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, outputUri)
            startActivityForResult(intent, Constants.REQUEST_CAMERA)
        }
        upload_photo_button.setOnClickListener {
            val intent = Intent()
            intent.type = "image*/
/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.REQUEST_GALLERY)
        }
*/
        chevron.setOnClickListener {
            if (see_more.isVisible) {
                see_more.toGone()
                chevron.animate().scaleY(1f).setDuration(300L).setStartDelay(350L).start()
            } else {
                see_more.toVisible()
                chevron.animate().scaleY(-1f).setDuration(350L).start()
            }
        }
    }

    private fun writeFields() {
        f_name.setText(userModel.firstName ?: "")
        l_name.setText(userModel.lastName ?: "")
        email.setText(userModel.email ?: "")
        university.setText(userModel.university ?: "")
        study_programme.setText(userModel.studyProgramme ?: "")
        study_year.setText(userModel.studyYear?.toString() ?: "")
    }

    private fun readFields() {
        userModel.firstName = f_name.text.toString().takeIf { it.isNotBlank() }
        userModel.lastName = l_name.text.toString().takeIf { it.isNotBlank() }
        userModel.email = email.text.toString().takeIf { it.isNotBlank() }
        userModel.university = university.text.toString().takeIf { it.isNotBlank() }
        userModel.studyProgramme = study_programme.text.toString().takeIf { it.isNotBlank() }
        userModel.studyYear = study_year.text.toString().takeIf { it.isNotBlank() }?.toInt()
    }

    private fun uploadUser() {
        loading_v.show()
        loading_v.message = "Uploading"
        requestManager.userUpdate(userModel) { response ->
            response?.let {
                toast("Upload finished")
                if (response.success) {
                    UserData.Session.userModel = userModel
                    loading_v.message = "Success"
                    toast("Request success")
                    //TODO: Show success view
//                    intent.putExtra(Constants.EXTRA_PARAM_USER_EDIT_RESULT, true)
//                    logo.postDelayed({ onBackPressed() }, 1000)
                } else {
                    loading_v.hide()
                    toast("Request failure")
                    //TODO: Show failure view; hide loading view
                }
            } ?: run {
                loading_v.hide()
                toast("Upload failed")
                //TODO: Show connection failure message
            }
        }
    }

    private fun initializeFacebookAuthorization() {
        facebook_connect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LoginManager.getInstance().logInWithReadPermissions(this, emptyList())
            } else {
                FirebaseAuth.getInstance().currentUser?.unlink("facebook.com")
            }
        }
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        toast("succeeded Facebook auth")
                        FirebaseAuth.getInstance().currentUser?.linkWithCredential(FacebookAuthProvider.getCredential(loginResult.accessToken.token))?.addOnCompleteListener(this@ProfileActivity)
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

    private fun initializeGoogleAuthorization() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.google_web_client_id)).requestEmail().build()
        val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        google_connect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startActivityForResult(googleSignInClient.signInIntent, 9001)
            } else {
                FirebaseAuth.getInstance().currentUser?.unlink("google.com")
            }
        }
    }

    private fun initializePhoneAuthorizationLayout() {
//        country_code_field.setText(getUserCountryCode())
        phone_connect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val phoneNumber = ("+37367123575").takeIf { it.isNotBlank() } ?: return@setOnCheckedChangeListener
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
//                imm?.hideSoftInputFromWindow(.windowToken, 0)

                PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                        toast("succeeded Phone auth")
                        FirebaseAuth.getInstance().currentUser?.linkWithCredential(phoneAuthCredential)?.addOnCompleteListener(this@ProfileActivity)
                    }

                    override fun onVerificationFailed(firebaseException: FirebaseException?) {
                        toast("failed Phone auth")
                        firebaseException?.printStackTrace()
                        //TODO: Handle failed Phone auth!
                    }
                })
            } else {
                FirebaseAuth.getInstance().currentUser?.unlink("phone")
            }
        }
    }

    override fun onComplete(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            val providers = FirebaseAuth.getInstance().currentUser?.providerData
            userModel.phone = providers?.find { it.providerId == "phone" }?.phoneNumber
            userModel.facebookId = providers?.find { it.providerId == "facebook.com" }?.uid
            userModel.googleId = providers?.find { it.providerId == "google.com" }?.uid
            uploadUser()
        } else {
            toast("Firebase auth task failed")
            //TODO: Handle failed Firebase auth!
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                toast("succeeded Google auth")
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                userModel.googleId = credential?.provider
                FirebaseAuth.getInstance().currentUser?.linkWithCredential(credential)?.addOnCompleteListener(this)
            } catch (e: ApiException) {
                toast("failed Google auth")
                e.printStackTrace()
            }
        } else {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }
}
