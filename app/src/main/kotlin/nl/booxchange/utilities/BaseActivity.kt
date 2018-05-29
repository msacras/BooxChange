package nl.booxchange.utilities

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import nl.booxchange.BooxchangeApp
import nl.booxchange.R
import nl.booxchange.api.APIClient
import nl.booxchange.screens.SignInActivity
import nl.booxchange.widget.LoadingView
import nl.booxchange.widget.RetryView
import org.jetbrains.anko.contentView
import org.jetbrains.anko.dip
import org.jetbrains.anko.findOptional


open class BaseActivity: AppCompatActivity() {
    protected val requestManager = APIClient.RequestManager(this)
/*
    private var rootView: ConstraintLayout? = null
        set(value) {
            field = value
//            field?.addView(tintView, ConstraintLayout.LayoutParams(-1, -1))
            field?.addView(loadingView, ConstraintLayout.LayoutParams(-1, -1))
            field?.addView(retryView, ConstraintLayout.LayoutParams(-1, -1))
            attachKeyboardListeners()
        }
*/
/*
    private val tintView: View by lazy {
        View(this).apply {
            setBackgroundColor(Color.BLACK)
            alpha = 0f
            id = R.id.tint_view
        }
    }
*/
    val loadingView by lazy { LoadingView(this) }
    val retryView by lazy { RetryView(this) }

/*
    private val previousActivity
        get() = with(BooxchangeApp.delegate.activityStack) {
            getOrNull(indexOf(this@BaseActivity) - 1)
        }
*/

//    private val isDismissableActivity by lazy { this is NavigationActivity || this is SignInActivity }
//    private var isDragging = false

/*
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (!(isDismissableActivity)) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> if (event.rawX < dip(10)) isDragging = true
                MotionEvent.ACTION_MOVE -> if (isDragging) {
                    window.decorView.rootView.translationX = event.rawX
                    previousActivity?.let {
                        it.window.decorView.rootView.translationX = .2f * window.decorView.rootView.translationX - .2f * it.window.decorView.rootView.measuredWidth
                        it.tintView.alpha = (1f - window.decorView.rootView.translationX / window.decorView.rootView.measuredWidth) * .5f
                    }
                }
                MotionEvent.ACTION_UP -> if (isDragging) {
                    val progress = (window.decorView.rootView.translationX / window.decorView.rootView.measuredWidth).coerceIn(0f, 1f)
                    if (progress < .35f) {
                        ValueAnimator.ofFloat(window.decorView.rootView.translationX, 0f).apply {
                            addUpdateListener {
                                val posX = it.animatedValue as Float
                                window.decorView.rootView.translationX = posX
                                previousActivity?.let {
                                    it.window.decorView.rootView.translationX = .2f * window.decorView.rootView.translationX - .2f * it.window.decorView.rootView.measuredWidth
                                    it.tintView.alpha = (1f - window.decorView.rootView.translationX / window.decorView.rootView.measuredWidth) * .5f
                                }
                            }
                            duration = (200 * progress).toLong()
                            interpolator = DecelerateInterpolator()
                            start()
                        }
                    } else {
                        ValueAnimator.ofFloat(window.decorView.rootView.translationX, window.decorView.rootView.measuredWidth.toFloat()).apply {
                            addUpdateListener {
                                val posX = it.animatedValue as Float
                                window.decorView.rootView.translationX = posX
                                previousActivity?.let {
                                    it.window.decorView.rootView.translationX = .2f * window.decorView.rootView.translationX - .2f * it.window.decorView.rootView.measuredWidth
                                    it.tintView.alpha = (1f - window.decorView.rootView.translationX / window.decorView.rootView.measuredWidth) * .5f
                                }
                            }
                            duration = (350 - 350 * progress).toLong()
                            interpolator = DecelerateInterpolator()
                            window.decorView.rootView.postDelayed({ onBackPressed() }, (350 - 350 * progress).toLong())
                            start()
                        }
                    }
                    isDragging = false
                }
            }
        }
        return if (!isDragging) super.dispatchTouchEvent(event) else true
    }
*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BooxchangeApp.delegate.activityStack.add(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        findOptional<View>(R.id.back_button)?.setOnClickListener { onBackPressed() }
//        overridePendingTransition(R.anim.activity_slide_in, 0)
/*
        if (!(isDismissableActivity)) {
            ValueAnimator.ofFloat(previousActivity?.window?.decorView?.rootView?.measuredWidth?.toFloat() ?: 0f, 0f).apply {
                addUpdateListener {
                    val posX = it.animatedValue as Float
                    previousActivity?.let {
                        it.window.decorView.rootView.translationX = .2f * posX - .2f * it.window.decorView.rootView.measuredWidth
                        it.tintView.alpha = (1f - posX / window.decorView.rootView.measuredWidth) * .5f
                    }
                }
                duration = 200L
                startDelay = 200L
                interpolator = DecelerateInterpolator()
                start()
            }
        }
*/
    }

/*
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        rootView = (contentView as? ConstraintLayout) ?: return
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        rootView = (view as? ConstraintLayout) ?: return
    }

    fun setRootView(view: View?) {
        rootView = (view as? ConstraintLayout) ?: return
    }
*/

    override fun onDestroy() {
        super.onDestroy()
        BooxchangeApp.delegate.activityStack.remove(this)
/*
        if (keyboardListenersAttached) {
            rootView?.viewTreeObserver?.removeOnGlobalLayoutListener(keyboardLayoutListener)
        }
*/
    }

/*
    private var currentViewHeight = 0
    private val keyboardLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val newHeight = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
        val heightDiff = currentViewHeight - newHeight

        if (currentViewHeight != newHeight) {
            when {
                heightDiff < -100 -> onHideKeyboard()
                heightDiff > 100 -> onShowKeyboard(heightDiff)
            }
            currentViewHeight = newHeight
        }
    }

    private var keyboardListenersAttached = false

    protected fun onShowKeyboard(keyboardHeight: Int) {}
    protected fun onHideKeyboard() {
        window.currentFocus?.clearFocus()
    }
*/

/*
    protected fun attachKeyboardListeners() {
        if (keyboardListenersAttached) {
            return
        }

        currentViewHeight = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
        rootView?.viewTreeObserver?.addOnGlobalLayoutListener(keyboardLayoutListener)

        keyboardListenersAttached = true
    }
*/

/*
    override fun onBackPressed() {
        if (isDismissableActivity) {
            super.onBackPressed()
        } else {
            val progress = (window.decorView.rootView.translationX / window.decorView.rootView.measuredWidth).coerceIn(0f, 1f)
            ValueAnimator.ofFloat(window.decorView.rootView.translationX, window.decorView.rootView.measuredWidth.toFloat()).apply {
                addUpdateListener {
                    window.decorView.rootView.translationX = it.animatedValue as Float
                    previousActivity?.let {
                        it.window.decorView.rootView.translationX = .2f * window.decorView.rootView.translationX - .2f * it.window.decorView.rootView.measuredWidth
                        it.tintView.alpha = (1f - window.decorView.rootView.translationX / window.decorView.rootView.measuredWidth) * .5f
                    }
                }
                duration = (350 - 350 * progress).toLong()
                interpolator = DecelerateInterpolator()
                window.decorView.rootView.postDelayed({ super.onBackPressed() }, (350 - 350 * progress).toLong())
                start()
            }
        }
    }
*/
}
