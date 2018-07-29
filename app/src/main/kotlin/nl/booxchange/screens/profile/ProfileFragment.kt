package nl.booxchange.screens.profile

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.databinding.adapters.TextViewBindingAdapter.setText
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Color.RED
import android.graphics.Color.WHITE
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.transition.Visibility
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.bumptech.glide.Glide
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.ProfilePictureView
import com.github.mikephil.charting.charts.Chart.LOG_TAG
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.vcristian.combus.dismiss
import kotlinx.android.synthetic.main.fragment_profile.*
import nl.booxchange.R
import nl.booxchange.R.color.midGray
import nl.booxchange.api.APIClient
import nl.booxchange.extension.*
import nl.booxchange.model.UserModel
import nl.booxchange.screens.BottomSheetDialog
import nl.booxchange.screens.MainFragmentActivity
import nl.booxchange.screens.SignInActivity
import nl.booxchange.utilities.UserData
import nl.booxchange.utilities.setErrorText
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.TimeUnit


class ProfileFragment : Fragment(), OnCompleteListener<AuthResult> {
    private val facebookCallbackManager = CallbackManager.Factory.create()
    private val userModel by lazy { UserData.Session.userModel?.copy() ?: UserModel("") }

    var imageuri: Uri? = null
        set(value) {
            field = value
            value?.let { Glide.with(context!!).load(value).into(profile_image) }
        }

    val color = Color.parseColor("#939393")
    val green = Color.parseColor("#17bd90")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        writeFields()
        initializeLayout()
        university.setText("Hogeschool van Amsterdam")

        listOf(chevron, edit_btn).forEach { it.setColorFilter(color) }
        gallery.setColorFilter(green)

        gallery.setOnClickListener {
            val bottomSheet = BottomSheetDialog()
            bottomSheet.show(fragmentManager, null)
        }

        val providers = FirebaseAuth.getInstance().currentUser?.providerData
        userModel.phoneId = providers?.find { it.providerId == "phone" }?.phoneNumber
        userModel.facebookId = providers?.find { it.providerId == "facebook.com" }?.uid
        userModel.googleId = providers?.find { it.providerId == "google.com" }?.uid

        initializeFacebookAuthorization(userModel.facebookId != null)
        initializeGoogleAuthorization(userModel.googleId != null)
        initializePhoneAuthorizationLayout(userModel.phoneId != null)


    }

    private fun initializeLayout() {
        edit_btn.setOnClickListener {
            TransitionManager.beginDelayedTransition(constraint_layout, AutoTransition())
            email.isEnabled = !email.isEnabled
            listOf(f_name, l_name).forEach { it.isEnabled = email.isEnabled }
            listOf(see_more, tick_btn, gallery, gallery_btn_des).forEach { it.setVisible(email.isEnabled) }
            if (email.isEnabled) {
                chevron.animate().scaleY(-1f).setDuration(350L).start()
                edit_btn.setImageResource(R.drawable.close_button)
            } else {
                chevron.animate().scaleY(1f).setDuration(300L).setStartDelay(350L).start()
                edit_btn.setImageResource(R.drawable.create_new_pencil_button)
                writeFields()
            }
        }
        tick_btn.setOnClickListener {
            if (TextUtils.isEmpty(f_name.text.toString())){
                label_f_name.error = "empty!!!"
            } else {
                label_f_name.error = null
            }
            TransitionManager.beginDelayedTransition(constraint_layout, AutoTransition())
            listOf(f_name, l_name, email).forEach { it.isEnabled = false }
            chevron.animate().scaleY(1f).setDuration(300L).setStartDelay(350L).start()
            listOf(tick_btn, see_more, gallery, gallery_btn_des).forEach { it.toGone() }
            edit_btn.setImageResource(R.drawable.create_new_pencil_button)
            readFields()
            uploadUser()
        }

        chevron.setOnClickListener {
            TransitionManager.beginDelayedTransition(constraint_layout, AutoTransition())
            if (see_more.isVisible) {
                listOf(see_more).forEach { it.toGone() }
                chevron.animate().scaleY(1f).setDuration(300L).setStartDelay(350L).start()
            } else {
                listOf(see_more).forEach { it.toVisible() }
                chevron.animate().scaleY(-1f).setDuration(350L).start()
            }
        }
    }

    private fun writeFields() {
        f_name.setText(userModel.firstName ?: "")
        l_name.setText(userModel.lastName ?: "")
        email.setText(userModel.email ?: "")
        university.setText(userModel.university ?: "")
    }

    private fun readFields() {
        userModel.firstName = f_name.text.toString().takeIf { it.isNotBlank() }
        userModel.lastName = l_name.text.toString().takeIf { it.isNotBlank() }
        userModel.email = email.text.toString().takeIf { it.isNotBlank() }
        userModel.university = university.text.toString().takeIf { it.isNotBlank() }
        //userModel.photo = progress_bar.text.toString().takeIf { it.isNotBlank() }
    }

    private fun uploadUser() {
        APIClient.User.userUpdate(userModel) { response ->
            response?.let {
                toast("Upload finished!")
//                if (response.success) {
                    UserData.Session.userModel = userModel
                    toast("Request success!")
                    //TODO: Show success view
//                } else {
//                    loading_v.hide()
                    toast("Request failure!")
                    //TODO: Show failure view; hide loading view
//                }
            } ?: run {
                toast("Upload failed!")
                //TODO: Show connection failure message
            }
        }
    }

    private fun initializeFacebookAuthorization(isLoggedIn: Boolean) {
        facebook_connect.isChecked = isLoggedIn


        facebook_connect.setOnCheckedChangeListener { _, isChecked ->
            val pd = ProgressDialog.show(context, null, null)
            pd.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            pd.setContentView(R.layout.my_progress)
            if (isChecked) {
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile, email"))
                pd.dismiss()
            } else {
                LoginManager.getInstance().logOut()
                FirebaseAuth.getInstance().currentUser?.unlink("facebook.com")?.addOnSuccessListener {
                    updateSwitchesAvailability()
                    pd.dismiss()
                }
            }
        }
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        toast("succeeded Facebook auth")
                        FirebaseAuth.getInstance().currentUser?.linkWithCredential(FacebookAuthProvider.getCredential(loginResult.accessToken.token))?.addOnCompleteListener(this@ProfileFragment)
                        val request  = GraphRequest.newMeRequest(
                                loginResult.accessToken,
                                object:GraphRequest.GraphJSONObjectCallback {
                                    override fun onCompleted(json:JSONObject, response:GraphResponse) {
                                        email.setText(json.getString("email"))
                                        f_name.setText(json.getString("first_name"))
                                        l_name.setText(json.getString("last_name"))
                                        readFields()
                                        uploadUser()
                                    }
                                })
                        request.parameters = Bundle().apply { putString("fields", "email, first_name, last_name") }
                        request.executeAsync()
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

    private fun initializeGoogleAuthorization(isLoggedIn: Boolean) {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.google_web_client_id)).requestEmail().build()
        val googleSignInClient = GoogleSignIn.getClient(activity ?: return, googleSignInOptions)

        google_connect.isChecked = isLoggedIn

        google_connect.setOnCheckedChangeListener { _, isChecked ->
            val pd = ProgressDialog.show(context, null, null)
            pd.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            pd.setContentView(R.layout.my_progress)
            if (isChecked) {
                startActivityForResult(googleSignInClient.signInIntent, 9001)
                pd.dismiss()
            } else {
                FirebaseAuth.getInstance().currentUser?.unlink("google.com")?.addOnSuccessListener {
                    updateSwitchesAvailability()
                    pd.dismiss()
                }
            }
        }
    }

    private fun initializePhoneAuthorizationLayout(isLoggedIn: Boolean) {
//        country_code_field.setText(getUserCountryCode())

        phone_connect.isChecked = isLoggedIn

        phone_connect.setOnCheckedChangeListener { _, isChecked ->
            val pd = ProgressDialog.show(context, null, null)
            pd.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            pd.setContentView(R.layout.my_progress)
            if (isChecked) {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?").show()
                val phoneNumber = ("+37367123575").takeIf { it.isNotBlank() } ?: return@setOnCheckedChangeListener
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
//                imm?.hideSoftInputFromWindow(.windowToken, 0)

                PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, activity ?: return@setOnCheckedChangeListener, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                        toast("succeeded Phone auth")
                        FirebaseAuth.getInstance().currentUser?.linkWithCredential(phoneAuthCredential)?.addOnCompleteListener(this@ProfileFragment)
                    }

                    override fun onVerificationFailed(firebaseException: FirebaseException?) {
                        toast("failed Phone auth")
                        firebaseException?.printStackTrace()
                        //TODO: Handle failed Phone auth!
                    }
                })
                pd.dismiss()
            } else {
                FirebaseAuth.getInstance().currentUser?.unlink("phone")?.addOnSuccessListener {
                    updateSwitchesAvailability()
                    pd.dismiss()
                }
            }
        }
    }

    override fun onComplete(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            val providers = FirebaseAuth.getInstance().currentUser?.providerData
            userModel.phoneId = providers?.find { it.providerId == "phone" }?.phoneNumber
            userModel.facebookId = providers?.find { it.providerId == "facebook.com" }?.uid
            userModel.googleId = providers?.find { it.providerId == "google.com" }?.uid
            uploadUser()
        } else {
            toast("Firebase auth task failed")
            //TODO: Handle failed Firebase auth!
        }
        updateSwitchesAvailability()
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

    fun updateSwitchesAvailability() {



        if (FirebaseAuth.getInstance().currentUser?.providerData?.size ?: 0 < 3) {
            facebook_connect.isEnabled = !facebook_connect.isChecked
            google_connect.isEnabled = !google_connect.isChecked
            phone_connect.isEnabled = !phone_connect.isChecked
        } else {
            facebook_connect.isEnabled = true
            google_connect.isEnabled = true
            phone_connect.isEnabled = true
        }
    }
}

fun Fragment.toast(text: String) = context?.toast(text)
