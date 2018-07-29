package nl.booxchange.screens.profile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v4.widget.TextViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.bumptech.glide.Glide
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
import kotlinx.android.synthetic.main.activity_main_fragment.*
import kotlinx.android.synthetic.main.fragment_profile.*
import nl.booxchange.R
import nl.booxchange.api.APIClient
import nl.booxchange.extension.isVisible
import nl.booxchange.extension.setVisible
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
import nl.booxchange.model.UserModel
import nl.booxchange.utilities.BaseActivity
import nl.booxchange.utilities.UserData
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit


class ProfileFragment : Fragment(), OnCompleteListener<AuthResult> {
    private val facebookCallbackManager = CallbackManager.Factory.create()
    private val userModel: UserModel = UserData.Session.userModel!!.copy()

    var imageuri: Uri? = null
        set(value) {
            field = value
            value?.let { Glide.with(context!!).load(value).into(profile_image) }
        }

    private val requestManager = APIClient.RequestManager(BaseActivity())

    val color = Color.parseColor("#939393")
    val colorBlue = Color.parseColor("#33cccc")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        writeFields()
        initializeLayout()

        (activity as MainFragmentActivity).setTitle("Profile")
        TextViewCompat.setTextAppearance((activity as MainFragmentActivity).toolbar_title, R.style.restPage)
        (activity as MainFragmentActivity).log_out.toVisible()
        (activity as MainFragmentActivity).log_out.setOnClickListener {
            UserData.Authentication.logout()
            val intent = Intent(activity, SignInActivity::class.java)
            startActivity(intent)
        }

        listOf(chevron, edit_btn, gallery).forEach { it.setColorFilter(color) }
        listOf(tick_btn).forEach { it.setColorFilter(colorBlue) }

        gallery.setOnClickListener {
            val bottomSheet = BottomSheetDialog()
            bottomSheet.show(fragmentManager, null)
        }

        val myString = arrayOf("A", "B", "C", "D")
        mySpinner.adapter = ArrayAdapter(activity, R.layout.spinner_item, myString)
        mySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                toast(myString[position])
            }
        }

        val providers = FirebaseAuth.getInstance().currentUser?.providerData
        userModel.phone = providers?.find { it.providerId == "phone" }?.phoneNumber
        userModel.facebookId = providers?.find { it.providerId == "facebook.com" }?.uid
        userModel.googleId = providers?.find { it.providerId == "google.com" }?.uid

        initializeFacebookAuthorization(userModel.facebookId != null)
        initializeGoogleAuthorization(userModel.googleId != null)
        initializePhoneAuthorizationLayout(userModel.phone != null)
    }

    private fun initializeLayout() {

        edit_btn.setOnClickListener {
            TransitionManager.beginDelayedTransition(constraint_layout, AutoTransition())
            email.isEnabled = !email.isEnabled
            listOf(f_name, l_name, university, study_programme, study_year).forEach { it.isEnabled = email.isEnabled }
            listOf(see_more, tick_btn, gallery).forEach { it.setVisible(email.isEnabled) }
            if (email.isEnabled) {
                chevron.animate().scaleY(-1f).setDuration(350L).start()
                edit_btn.setImageResource(R.mipmap.ic_cancel)
            } else {
                chevron.animate().scaleY(1f).setDuration(300L).setStartDelay(350L).start()
                edit_btn.setImageResource(R.drawable.ic_edit_btn)
                writeFields()
            }


//            UserData.Persistent.write("user wants to receive push notifications?", true)
        }

        tick_btn.setOnClickListener {
            TransitionManager.beginDelayedTransition(constraint_layout, AutoTransition())
            chevron.animate().scaleY(1f).setDuration(300L).setStartDelay(350L).start()
            listOf(tick_btn, see_more, gallery).forEach { it.toGone() }
            listOf(f_name, l_name, email, university, study_programme, study_year).forEach { it.isEnabled = false }
            edit_btn.setImageResource(R.drawable.ic_edit_btn)
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
                toast("Upload finished!")
                if (response.success) {
                    UserData.Session.userModel = userModel
                    loading_v.message = "Success"
                    toast("Request success!")
                    //TODO: Show success view
                } else {
                    loading_v.hide()
                    toast("Request failure!")
                    //TODO: Show failure view; hide loading view
                }
            } ?: run {
                loading_v.hide()
                toast("Upload failed!")
                //TODO: Show connection failure message
            }
        }
    }

    private fun initializeFacebookAuthorization(isLoggedIn: Boolean) {
        facebook_connect.isChecked = isLoggedIn

        facebook_connect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LoginManager.getInstance().logInWithReadPermissions(this, emptyList())
            } else {
                FirebaseAuth.getInstance().currentUser?.unlink("facebook.com")?.addOnSuccessListener {
                    updateSwitchesAvailability()
                }
            }
        }
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        toast("succeeded Facebook auth")
                        FirebaseAuth.getInstance().currentUser?.linkWithCredential(FacebookAuthProvider.getCredential(loginResult.accessToken.token))?.addOnCompleteListener(this@ProfileFragment)
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
            if (isChecked) {
                startActivityForResult(googleSignInClient.signInIntent, 9001)
            } else {
                FirebaseAuth.getInstance().currentUser?.unlink("google.com")?.addOnSuccessListener {
                    updateSwitchesAvailability()
                }
            }
        }
    }

    private fun initializePhoneAuthorizationLayout(isLoggedIn: Boolean) {
//        country_code_field.setText(getUserCountryCode())

        phone_connect.isChecked = isLoggedIn

        phone_connect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
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
            } else {



                FirebaseAuth.getInstance().currentUser?.unlink("phone")?.addOnSuccessListener {



                    updateSwitchesAvailability()
                }
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
