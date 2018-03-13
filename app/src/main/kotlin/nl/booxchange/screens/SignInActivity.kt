package nl.booxchange.screens

import android.os.Bundle
import nl.booxchange.R
import nl.booxchange.utilities.BaseActivity
import com.google.firebase.FirebaseException
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.startActivity
import java.util.concurrent.TimeUnit
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import org.jetbrains.anko.toast
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInApi
import com.google.firebase.auth.*
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ApiException
import android.R.attr.data
import android.support.constraint.ConstraintSet
import android.support.design.widget.CoordinatorLayout
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat.startActivity
import android.transition.Visibility
import android.util.Log
import android.view.View
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible


/**
 * Created by Cristian Velinciuc on 3/12/18.
 */
class SignInActivity : BaseActivity(), OnCompleteListener<AuthResult> {
  private val facebookCallbackManager = CallbackManager.Factory.create()


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_sign_in)
    initializeFacebookAuthorization()
    initializeGoogleAuthorization()
    initializePhoneAuthorizationLayout()
  }

  private fun initializeFacebookAuthorization() {
    facebook_sign_in_button.setOnClickListener {
      hideViews {
        LoginManager.getInstance().logInWithReadPermissions(this, emptyList())
      }
    }
    LoginManager.getInstance().registerCallback(facebookCallbackManager,
      object : FacebookCallback<LoginResult> {
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
    phone_sign_in_button.setOnClickListener {
      val phoneNumber = phone_number_field.text.toString().takeIf { it.isNotBlank() } ?: return@setOnClickListener

      hideViews {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phone_number_field.text.toString(), 60, TimeUnit.SECONDS, this, object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
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

  override fun onComplete(task: Task<AuthResult>) {
    if (task.isSuccessful) {
      toast("Firebase auth task succeeded")
      startActivity<HomepageActivity>()
      finish()
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
      setAlpha(R.id.phone_sign_in_button, 0.0f)
      setAlpha(R.id.phone_number_field, 0.0f)
      setAlpha(R.id.phone_icon, 0.0f)
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
