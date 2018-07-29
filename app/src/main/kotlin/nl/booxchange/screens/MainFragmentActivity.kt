package nl.booxchange.screens

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.databinding.ObservableField
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageButton
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.vcristian.combus.expect
import com.vcristian.combus.post
import kotlinx.android.synthetic.main.activity_main_fragment.*
import nl.booxchange.BooxchangeApp
import nl.booxchange.R
import nl.booxchange.R.color.midGray
import nl.booxchange.R.id.*
import nl.booxchange.R.layout.fragment_profile
import nl.booxchange.extension.getColorCompat
import nl.booxchange.extension.isVisible
import nl.booxchange.extension.setVisible
import nl.booxchange.model.BookModel
import nl.booxchange.model.ChatOpenedEvent
import nl.booxchange.model.MessageReceivedEvent
import nl.booxchange.model.OverlayFragment
import nl.booxchange.screens.book.BookFragment
import nl.booxchange.screens.book.BookFragmentViewModel
import nl.booxchange.screens.library.LibraryFragment
import nl.booxchange.screens.library.LibraryFragmentViewModel
import nl.booxchange.screens.profile.ProfileFragment
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
    val viewModel = LibraryFragmentViewModel()
    var library = LibraryFragmentViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_fragment)

        //setSupportActionBar(toolbar)

        log_out.setColorFilter(midGray)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false)
        toolbar.setTitle("")
        toolbar.setSubtitle("")

        log_out.visibility = GONE

        log_out.setOnClickListener {
            UserData.Authentication.logout()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        screens.forEachIndexed { currentSelectedIndex, (button, tag) ->
            button.setOnClickListener {
                showFragment(tag)
                if (tag.equals("profile_page")) {
                    log_out.visibility = VISIBLE
                }else {
                    log_out.visibility = GONE
                }
                if (tag.equals("library_page")) {
                    add_book.visibility = VISIBLE
                }else {
                    add_book.visibility = GONE
                }
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
            Constants.FRAGMENT_CHAT -> post(ChatOpenedEvent(chatId = intent.getStringExtra(Constants.EXTRA_PARAM_CHAT_ID)))
            else -> home_page.performClick()
        }

        expect(MessageReceivedEvent::class.java) { (messageModel) ->
            if (messageModel.userId == UserData.Session.userId) return@expect
            //TODO: unread messages counter icon

            messageModel.content
        }
    }

    private val blackColor by lazy { getColorCompat(R.color.mountainMeadow) }
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

    override fun onResume() {
        super.onResume()
        BooxchangeApp.isInForeground = true
    }

    override fun onPause() {
        super.onPause()
        BooxchangeApp.isInForeground = false
    }

    override fun onBackPressed() {
        if (supportFragmentManager.fragments.all {
                (it as? BaseFragment)?.onBackPressed() != false
            }) {
            super.onBackPressed()
        }
    }
}
