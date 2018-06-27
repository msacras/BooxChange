package nl.booxchange.screens

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageButton
import com.vcristian.combus.expect
import kotlinx.android.synthetic.main.activity_main_fragment.*
import nl.booxchange.BooxchangeApp
import nl.booxchange.R
import nl.booxchange.R.id.*
import nl.booxchange.extension.getColorCompat
import nl.booxchange.model.MessageReceivedEvent
import nl.booxchange.model.OverlayFragment
import nl.booxchange.utilities.BaseFragment
import nl.booxchange.utilities.Constants
import nl.booxchange.utilities.UserData

class MainFragmentActivity: AppCompatActivity() {
    val screens by lazy {listOf (
        home_page to "home_page",
        message_page to "message_page",
        library_page to "library_page",
        profile_page to "profile_page"
    )}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BooxchangeApp.mainActivityDelegate = this
        setContentView(R.layout.activity_main_fragment)

        setSupportActionBar(toolbar)

        screens.forEachIndexed { currentSelectedIndex, (button, tag) ->
            button.setOnClickListener {
                showFragment(tag)
                fragment_title.text = tag.split("_").first().capitalize()
                screens.forEach { (otherButton, _) ->
                    val color = if (otherButton == button) blackColor else grayColor
                    updateColor(otherButton as AppCompatImageButton, color)
                    app_bar_layout.setExpanded(true, true)
                }
                val targetPositionX = (currentSelectedIndex * focused_button_highlight.width).toFloat()
                val transitionDuration = (Math.abs(focused_button_highlight.translationX - targetPositionX) / (focused_button_highlight.width * 3)) * 450
                focused_button_highlight.animate().translationX(targetPositionX).setDuration(transitionDuration.toLong()).start()
            }
        }

        when (intent.getStringExtra(Constants.EXTRA_PARAM_TARGET_VIEW)) {
            Constants.FRAGMENT_PROFILE -> profile_page.performClick()
            else -> home_page.performClick()
        }

        expect(MessageReceivedEvent::class.java) { (messageModel) ->
            if (messageModel.userId == UserData.Session.userId) return@expect
            //TODO: unread messages counter icon

            messageModel.content
        }
    }

    private val blackColor by lazy { getColorCompat(R.color.black) }
    private val grayColor by lazy { getColorCompat(R.color.midGray) }
    private val colorEvaluator = ArgbEvaluator()

    private fun updateColor(button: AppCompatImageButton, toColor: Int) {
        val fromColor = button.tag as? Int ?: grayColor
        ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                val currentColor = colorEvaluator.evaluate(it.animatedFraction, fromColor, toColor) as Int
                button.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                button.tag =  currentColor
            }
            duration = 150
            start()
        }
    }

    fun showFragment(tag: String, exclusive: Boolean = true) {
        supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).apply {
            supportFragmentManager.fragments.forEach { fragment ->
                if (fragment.tag == tag) show(fragment) else if (exclusive) hide(fragment)
            }
            commit()
        }
    }

    fun hideFragment(targetTag: String) {
        val fragment = supportFragmentManager.findFragmentByTag(targetTag)
        supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).hide(fragment).commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.fragments.all {
                (it as? BaseFragment)?.onBackPressed() != false
            }) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        BooxchangeApp.mainActivityDelegate = null
        super.onDestroy()
    }
}
