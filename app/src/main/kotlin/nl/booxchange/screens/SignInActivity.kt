package nl.booxchange.screens

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_sign_in.*
import nl.booxchange.R
import nl.booxchange.R.id.*
import nl.booxchange.extension.getColorCompat
import nl.booxchange.extension.withExitSymbol
import nl.booxchange.utilities.Constants
import nl.booxchange.utilities.UserData
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit


/**
 * Created by Cristian Velinciuc on 3/12/18.
 */
class SignInActivity: AppCompatActivity(), OnCompleteListener<AuthResult> {
  private val facebookCallbackManager = CallbackManager.Factory.create()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_sign_in)
    initializeLayout()
    initializeFacebookAuthorization()
    initializeGoogleAuthorization()
    initializePhoneAuthorizationLayout()
  }

  private fun initializeLayout() {
    progress_bar.indeterminateDrawable.setColorFilter(getColorCompat(R.color.lightGray), PorterDuff.Mode.SRC_IN);
  }

  private fun initializeFacebookAuthorization() {
    facebook_sign_in_button.setOnClickListener {
      hideViews {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
      }
    }
    LoginManager.getInstance().registerCallback(facebookCallbackManager,
      object: FacebookCallback<LoginResult> {
        override fun onSuccess(loginResult: LoginResult) {
          toast("succeeded Facebook auth")
          FirebaseAuth.getInstance().signInWithCredential(FacebookAuthProvider.getCredential(loginResult.accessToken.token)).addOnCompleteListener(this@SignInActivity)
        }

        override fun onCancel() {
          toast("canceled Facebook auth")
          showViews()
          //TODO: Handle canceled Facebook auth!
        }

        override fun onError(exception: FacebookException) {
          toast("failed Facebook auth")
          showViews()
          exception.printStackTrace()
          //TODO: Handle failed Facebook auth!
        }
      })
  }

  private fun initializeGoogleAuthorization() {
    val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.google_web_client_id)).requestEmail().build()
    val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    google_sign_in_button.setOnClickListener {
      hideViews {
        startActivityForResult(googleSignInClient.signInIntent, 9001)
      }
    }
  }

  private fun initializePhoneAuthorizationLayout() {
    country_code_field.setText(getUserCountryCode())
    phone_sign_in_button.setOnClickListener {
      val phoneNumber = ("${country_code_field.text} ${phone_number_field.text}").takeIf { it.isNotBlank() } ?: return@setOnClickListener
      val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
      imm?.hideSoftInputFromWindow(phone_number_field.windowToken, 0)
      hideViews {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
          override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential?) {
            toast("succeeded Phone auth")
            FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential ?: return).addOnCompleteListener(this@SignInActivity)
          }

          override fun onVerificationFailed(firebaseException: FirebaseException?) {
            toast("failed Phone auth")
            showViews()
            firebaseException?.printStackTrace()
            //TODO: Handle failed Phone auth!
          }
        })
      }
    }
  }

  private fun getUserCountryCode(): String {
    val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    return (Constants.IDDC[telephonyManager?.simCountryIso?.toUpperCase()] ?: Constants.IDDC["NL"]!!).withExitSymbol
  }

  override fun onComplete(task: Task<AuthResult>) {
    if (task.isSuccessful) {
      toast("Firebase auth task succeeded")
      UserData.Authentication.register { isLoggedIn, isNewUser ->
        if (isLoggedIn) {
          toast("Database auth task succeeded")
          UserData.Session.fetchUserBooksList {}
          if (isNewUser) {
            startActivity<MainFragmentActivity>(Constants.EXTRA_PARAM_TARGET_VIEW to Constants.FRAGMENT_PROFILE)
          } else {
            startActivity<MainFragmentActivity>(Constants.EXTRA_PARAM_TARGET_VIEW to Constants.FRAGMENT_HOME)
          }
          finish()
        } else {
          toast("Database auth task failed")
          //TODO: Show failure message
          //TODO: Show help message if user fails to authenticate more than 3 times with the same credentials
          UserData.Authentication.logout()
          showViews()
        }
      }
    } else {
      toast("Firebase auth task failed")
      //TODO: Handle failed Firebase auth!
      showViews()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == 9001) {
      val task = GoogleSignIn.getSignedInAccountFromIntent(data)
      try {
        val account = task.getResult(ApiException::class.java)
        toast("succeeded Google auth")
        FirebaseAuth.getInstance().signInWithCredential(GoogleAuthProvider.getCredential(account.idToken, null)).addOnCompleteListener(this)
      } catch (e: ApiException) {
        toast("failed Google auth")
        showViews()
        e.printStackTrace()
      }
    } else {
      facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
    }
  }

  private fun showViews() {
    val animationStepOne = ConstraintSet().apply {
      clone(constraint_layout)
      setVisibility(R.id.app_name, View.VISIBLE)
      setHorizontalBias(R.id.app_logo, 0.2f)
      setVisibility(R.id.progress_bar, View.INVISIBLE)
      setVerticalBias(R.id.app_logo, 0.05f)
      setVerticalBias(R.id.phone_sign_in_button, 0.6f)
      setAlpha(R.id.country_code_field, 1f)
      setAlpha(R.id.phone_sign_in_button, 1.0f)
      setAlpha(R.id.phone_number_field, 1.0f)
      setAlpha(R.id.phone_icon, 1.0f)
    }

    TransitionManager.beginDelayedTransition(constraint_layout)
    animationStepOne.applyTo(constraint_layout)
  }

  private fun hideViews(onDone: () -> Unit) {
    val animationStepOne = ConstraintSet().apply {
      clone(constraint_layout)
      setVisibility(R.id.app_name, View.INVISIBLE)
      setHorizontalBias(R.id.app_logo, 0.5f)
    }
    val animationStepTwo = ConstraintSet().apply {
      clone(animationStepOne)
      setVerticalBias(R.id.app_logo, 0.5f)
      setVerticalBias(R.id.phone_sign_in_button, 1.0f)
      setAlpha(R.id.country_code_field, 0.0f)
      setAlpha(R.id.phone_sign_in_button, 0.0f)
      setAlpha(R.id.phone_number_field, 0.0f)
      setAlpha(R.id.phone_icon, 0.0f)
      setAlpha(R.id.google_txt, 0.0f)
      setAlpha(R.id.fb_txt, 0.0f)
      setAlpha(R.id.line_bottom, 0.0f)
      setAlpha(R.id.line_left, 0.0f)
      setAlpha(R.id.line_right, 0.0f)
      setAlpha(R.id.line_top1, 0.0f)
      setAlpha(R.id.line_top2, 0.0f)
      setAlpha(R.id.little_line1, 0.0f)
      setAlpha(R.id.little_line2, 0.0f)
      setAlpha(R.id.little_line3, 0.0f)
      setAlpha(R.id.little_line4, 0.0f)
    }
    val animationStepThree = ConstraintSet().apply {
      clone(animationStepTwo)
      setVisibility(R.id.progress_bar, View.VISIBLE)
    }

    TransitionManager.beginDelayedTransition(constraint_layout)
    animationStepOne.applyTo(constraint_layout)
    constraint_layout.postDelayed({
      TransitionManager.beginDelayedTransition(constraint_layout, AutoTransition().apply { duration = 600 })
      animationStepTwo.applyTo(constraint_layout)
      constraint_layout.postDelayed({
        TransitionManager.beginDelayedTransition(constraint_layout)
        animationStepThree.applyTo(constraint_layout)
        constraint_layout.postDelayed({
          TransitionManager.endTransitions(constraint_layout)
          onDone()
        }, 500)
      }, 1000)
    }, 700)
  }
}
