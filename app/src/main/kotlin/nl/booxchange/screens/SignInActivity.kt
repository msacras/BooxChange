package nl.booxchange.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_sign_in.*
import nl.booxchange.R
import nl.booxchange.extension.getColorCompat
import nl.booxchange.extension.withExitSymbol
import nl.booxchange.screens.library.LibraryFragment
import nl.booxchange.screens.library.SettingsActivity
import nl.booxchange.utilities.Constants
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit

class SignInActivity : AppCompatActivity(), OnCompleteListener<AuthResult> {
    private val facebookCallbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        //initializeLayout()
        initializeFacebookAuthorization()
        initializeGoogleAuthorization()
        initializePhoneAuthorizationLayout()
    }

    private fun initializeFacebookAuthorization() {
        facebook_sign_in_button.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
            hideKeyboard(phone_number_field)
        }

        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        toast("succeeded Facebook auth")
                        FirebaseAuth.getInstance().signInWithCredential(FacebookAuthProvider.getCredential(loginResult.accessToken.token)).addOnCompleteListener(this@SignInActivity)
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

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun initializeGoogleAuthorization() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.google_web_client_id)).requestEmail().build()
        val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        google_sign_in_button.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, 9001)
        }
    }

    private fun initializePhoneAuthorizationLayout() {
        country_code_field.setText(getUserCountryCode())
        phone_sign_in_button.setOnClickListener {
            val phoneNumber = ("${country_code_field.text} ${phone_number_field.text}").takeIf { it.isNotBlank() }
                    ?: return@setOnClickListener

            (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(phone_number_field.windowToken, 0)

            PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential?) {
                    toast("succeeded Phone auth")
                    FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential
                            ?: return).addOnCompleteListener(this@SignInActivity)
                }

                override fun onVerificationFailed(firebaseException: FirebaseException?) {
                    toast("failed Phone auth")
                    firebaseException?.printStackTrace()
                    //TODO: Handle failed Phone auth!
                }
            })
        }
    }

    private fun getUserCountryCode(): String {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

        return (Constants.IDDC[telephonyManager?.simCountryIso?.toUpperCase()]
                ?: Constants.IDDC["NL"]!!).withExitSymbol
    }

    override fun onComplete(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                FirebaseDatabase.getInstance().getReference("instances").child(it.token).setValue(FirebaseAuth.getInstance().currentUser?.uid
                        ?: return@addOnSuccessListener)
            }
            if (task.result.additionalUserInfo.isNewUser) {
                startActivity<MainFragmentActivity>(Constants.EXTRA_PARAM_TARGET_VIEW to Constants.FRAGMENT_LIBRARY)
            } else {
                startActivity<MainFragmentActivity>(Constants.EXTRA_PARAM_TARGET_VIEW to Constants.FRAGMENT_HOME)
            }
            finish()
        } else {
//      if ((task.exception as FirebaseAuthInvalidUserException).errorCode == "ERROR_USER_DISABLED") {}
            task.exception?.localizedMessage?.let { alert(it).show() }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                FirebaseAuth.getInstance().signInWithCredential(GoogleAuthProvider.getCredential(task.getResult(ApiException::class.java).idToken, null)).addOnCompleteListener(this)
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        } else {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

/*    private fun showViews() {
        val animSlide1 = AnimationUtils.loadAnimation(applicationContext, R.anim.activity_slide_up)
        constraint_layout.startAnimation(animSlide1)
        val animationStepOne = ConstraintSet().apply {
            clone(constraint_layout)
            setHorizontalBias(R.id.app_logo, 0.2f)
            setVisibility(R.id.progress_bar, View.INVISIBLE)
            setVisibility(R.id.app_name, View.VISIBLE)
        }
        val animationStepTwo = ConstraintSet().apply {
            clone(middle_const_layout)
            setAlpha(R.id.country_code_field, 1.0f)
            setAlpha(R.id.phone_sign_in_button, 1.0f)
            setAlpha(R.id.phone_number_field, 1.0f)
            setAlpha(R.id.phone_icon, 1.0f)
            setAlpha(R.id.little_line1, 1.0f)
            setAlpha(R.id.little_line2, 1.0f)
            setAlpha(R.id.or_sign_in_with_label, 1.0f)
            setAlpha(R.id.facebook_sign_in_button, 1.0f)
            setAlpha(R.id.google_sign_in_button, 1.0f)
        }
        TransitionManager.beginDelayedTransition(constraint_layout, AutoTransition().apply { duration = 500 })
        animationStepOne.applyTo(constraint_layout)
        constraint_layout.postDelayed({
            TransitionManager.beginDelayedTransition(middle_const_layout, AutoTransition().apply { duration = 600 })
            animationStepTwo.applyTo(middle_const_layout)}, 1000)

    }

    private fun hideViews(onDone: () -> Unit) {
        val animationStepOne = ConstraintSet().apply {
            clone(constraint_layout)
            setVisibility(R.id.app_name, View.INVISIBLE)
            setHorizontalBias(R.id.app_logo, 0.5f)
            Handler().postDelayed({
                val animSlide1 = AnimationUtils.loadAnimation(applicationContext, R.anim.activity_slide_down)
                constraint_layout.startAnimation(animSlide1)
            }, 800)
        }
        val animationStepTwo = ConstraintSet().apply {
            clone(middle_const_layout)
            setAlpha(R.id.country_code_field, 0.0f)
            setAlpha(R.id.phone_sign_in_button, 0.0f)
            setAlpha(R.id.phone_number_field, 0.0f)
            setAlpha(R.id.phone_icon, 0.0f)
            setAlpha(R.id.little_line1, 0.0f)
            setAlpha(R.id.little_line2, 0.0f)
            setAlpha(R.id.or_sign_in_with_label, 0.0f)
            setAlpha(R.id.facebook_sign_in_button, 0.0f)
            setAlpha(R.id.google_sign_in_button, 0.0f)
        }
        val animationStepThree = ConstraintSet().apply {
            clone(animationStepOne)
            setVisibility(R.id.progress_bar, View.VISIBLE)
        }

        TransitionManager.beginDelayedTransition(constraint_layout)
        animationStepOne.applyTo(constraint_layout)
        constraint_layout.postDelayed({
            TransitionManager.beginDelayedTransition(middle_const_layout, AutoTransition().apply { duration = 600 })
            animationStepTwo.applyTo(middle_const_layout)
            constraint_layout.postDelayed({
                TransitionManager.beginDelayedTransition(constraint_layout)
                animationStepThree.applyTo(constraint_layout)
                constraint_layout.postDelayed({
                    TransitionManager.endTransitions(middle_const_layout)
                    onDone()
                }, 500)
            }, 1000)
        }, 700)
    }*/
}
