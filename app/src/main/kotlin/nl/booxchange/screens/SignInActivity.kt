package nl.booxchange.screens

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.v4.widget.NestedScrollView
import android.telephony.TelephonyManager
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
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
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.list_item_country_code.view.*
import nl.booxchange.R
import nl.booxchange.extension.getColorById
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
import nl.booxchange.extension.withExitSymbol
import nl.booxchange.utilities.BaseActivity
import nl.booxchange.utilities.Constants
import nl.booxchange.utilities.UserData
import nl.booxchange.widget.LAnimationDrawable
import org.jetbrains.anko.dip
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue


/**
 * Created by Cristian Velinciuc on 3/12/18.
 */
class SignInActivity : BaseActivity(), OnCompleteListener<AuthResult> {
    private val facebookCallbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        initializeLayout()
        initializeFacebookAuthorization()
        initializeGoogleAuthorization()
        initializePhoneAuthorization()
    }

    private fun initializeLayout() {
        progress_view.background = LAnimationDrawable(thickness = dip(1).toFloat(), width = dip(50).toFloat(), primaryColor = getColorById(R.color.lightGray), secondaryColor = getColorById(R.color.whiteGray))
        country_code_list.postDelayed({
            country_code_scroller.setPadding(0, country_code_field.top, 0, country_code_field.top + country_code_field.height)
            (progress_view.background as AnimationDrawable).start()
        }, 100)

        var isSettling = false
        var isScrollerBeingTouched = false
        var isFlingScrollInProgress = false

        var scrollAnimator: ObjectAnimator? = null

        fun smoothScrollToOffset(offset: Int, delta: Int) {
            scrollAnimator?.cancel()
            scrollAnimator = ObjectAnimator.ofInt(country_code_scroller, "scrollY", offset).apply {
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        isSettling = false
                    }
                })
                interpolator = DecelerateInterpolator()
                duration = delta * 3L
                start()
            }
        }

        fun scrollToNearestItem() {
            if (isSettling) return
            isSettling = true
            val scrollY = country_code_scroller.scrollY
            val itemHeight = country_code_list.getChildAt(0).height
            val delta = scrollY % itemHeight
            val newScrollPosition = if (delta < itemHeight / 2) scrollY - delta else scrollY + (itemHeight - delta)
            smoothScrollToOffset(newScrollPosition, delta)
        }

        var index = 0
        Constants.IDDC.forEach { countryCode, numberCode ->
            val itemView = layoutInflater.inflate(R.layout.list_item_country_code, country_code_list, false)
            val iconResourcesId = resources.getIdentifier("ic_list_${countryCode.toLowerCase()}", "drawable", packageName)
            val selfIndex = index
            itemView.country_flag.setImageResource(iconResourcesId)
            itemView.country_code.text = "+$numberCode"
            itemView.setOnClickListener {
                val scrollY = country_code_scroller.scrollY
                val itemHeight = country_code_list.getChildAt(selfIndex).height
                val itemScrollOffset = itemHeight * selfIndex
                if (itemScrollOffset == scrollY) {
                    country_code_field.text = "+$numberCode"
                    country_code_scroller.toGone()
                } else {
                    smoothScrollToOffset(itemScrollOffset, (itemScrollOffset - scrollY).absoluteValue / 4)
                }
            }
            index++
            country_code_list.addView(itemView)
        }

        country_code_scroller.setOnScrollChangeListener { _: NestedScrollView, _, scrollY, _, oldScrollY ->
            val scrollDelta = (oldScrollY - scrollY).absoluteValue
            isFlingScrollInProgress = scrollDelta > 2
            if (!isScrollerBeingTouched && scrollDelta < 3) {
                scrollToNearestItem()
            }
        }

        country_code_scroller.setOnTouchListener { _, event ->
            isScrollerBeingTouched = event.action !in listOf(MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL)

            if (!(isScrollerBeingTouched || isFlingScrollInProgress)) {
                scrollToNearestItem()
            }

            country_code_scroller.onTouchEvent(event)
        }

        country_code_field.setOnClickListener {
            country_code_scroller.toVisible()
        }
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

    private fun initializePhoneAuthorization() {
        country_code_field.text = userCountryCode
        phone_sign_in_button.setOnClickListener {
            val phoneNumber = ("${country_code_field.text} ${phone_number_field.text}").takeIf { it.isNotBlank() } ?: return@setOnClickListener
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(phone_number_field.windowToken, 0)
            hideViews {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
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

    private val userCountryCode
        get() = (Constants.IDDC[(getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)?.simCountryIso?.toUpperCase()] ?: Constants.IDDC["NL"]!!).withExitSymbol

    override fun onComplete(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            toast("Firebase auth task succeeded")
            UserData.Authentication.register { isLoggedIn, isNewUser ->
                if (isLoggedIn) {
                    toast("Database auth task succeeded")
                    UserData.Session.fetchUserBooksList {}
                    if (isNewUser) {
                        startActivity<ProfileActivity>()
                    } else {
                        startActivity<HomepageActivity>()
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
