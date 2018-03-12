package nl.booxchange.screens

import android.os.Bundle
import android.util.Log.d
import nl.booxchange.R
import nl.booxchange.utilities.BaseActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.startActivity
import java.util.concurrent.TimeUnit
import java.util.Arrays.asList
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

/**
 * Created by Cristian Velinciuc on 3/12/18.
 */
class SignInActivity: BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_sign_in)
    initializeLayout()
    initializeFacebookAuthorization()
  }

  private fun initializeLayout() {
    sign_in_button.setOnClickListener {
      PhoneAuthProvider.getInstance().verifyPhoneNumber(phone_number_field.text.toString(), 60, TimeUnit.SECONDS, this, object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential?) {
          FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential ?: return).addOnCompleteListener { task ->
            if (task.isSuccessful) {
              startActivity<HomepageActivity>()
              finish()
            }
          }
          d("SignInActivity", "verification complete ${phoneAuthCredential.smsCode} ${phoneAuthCredential.provider}")
        }

        override fun onVerificationFailed(firebaseException: FirebaseException?) {
          firebaseException?.printStackTrace()
          d("SignInActivity", "verification failed")
        }
      })
    }
  }

  private fun initializeFacebookAuthorization() {
    val callbackManager = CallbackManager.Factory.create()

    LoginManager.getInstance().registerCallback(callbackManager,
      object: FacebookCallback<LoginResult> {
        override fun onSuccess(loginResult: LoginResult) {

        }

        override fun onCancel() {

        }

        override fun onError(exception: FacebookException) {

        }
      })
  }
}
