package nl.booxchange.screens

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.service.autofill.UserData
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageButton
import com.vcristian.combus.expect
import com.vcristian.combus.post
import kotlinx.android.synthetic.main.activity_main_fragment.*
import nl.booxchange.BooxchangeApp
import nl.booxchange.R
import nl.booxchange.R.id.*
import nl.booxchange.extension.getColorCompat
import nl.booxchange.extension.setVisible
import nl.booxchange.model.events.ChatOpenedEvent
import nl.booxchange.model.events.ChatsStateChangeEvent
import nl.booxchange.model.events.MessageReceivedEvent
import nl.booxchange.screens.settings.SettingsActivity
import nl.booxchange.utilities.BaseFragment
import nl.booxchange.utilities.Constants

class MainFragmentActivity: AppCompatActivity() {
    val screens by lazy {listOf (
        home_page to "booxchange",
        message_page to "Messages",
        library_page to "Library"
    )}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_fragment)

//        settings.setImageResource(R.drawable.ic_settings_icon)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.title = ""
        toolbar.subtitle = ""

        settings.setOnClickListener {
            val setting = Intent(this, SettingsActivity::class.java)
            startActivity(setting)
        }

        screens.forEach { (button, tag) ->
            button.setOnClickListener {
                showFragment(tag)

                settings.setVisible(tag == "Library")
                fragment_title.text = tag

                screens.forEach { (otherButton, _) ->
                    val color = if (otherButton == button) greenColor else whiteColor
                    updateColor(otherButton as AppCompatImageButton, color)
                }
            }
        }

        when (intent.getStringExtra(Constants.EXTRA_PARAM_TARGET_VIEW)) {
//            Constants.FRAGMENT_PROFILE -> profile_page.performClick()
            Constants.FRAGMENT_CHAT -> post(ChatOpenedEvent(chatId = intent.getStringExtra(Constants.EXTRA_PARAM_CHAT_ID)))
            else -> home_page.performClick()
        }

        expect(MessageReceivedEvent::class.java) { (messageModel) ->
            //TODO: unread messages counter icon

            messageModel.content
        }

        expect(ChatsStateChangeEvent::class.java) { (count) ->

        }
    }

    private val greenColor by lazy { getColorCompat(R.color.springGreen) }
    private val whiteColor by lazy { getColorCompat(R.color.whiteGray) }
//    private val darkGreenColor by lazy { getColorCompat(R.color.darkGreen) }
    private val colorEvaluator = ArgbEvaluator()

    private fun updateColor(button: AppCompatImageButton, toColor: Int) {
        val fromColor = button.tag as? Int ?: whiteColor
        ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                val currentColor = colorEvaluator.evaluate(it.animatedFraction, fromColor, toColor) as Int
                button.setBackgroundColor(currentColor)
                //button.tag =  currentColor
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
        supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).hide(supportFragmentManager.findFragmentByTag(targetTag)!!).commit()
    }

    override fun onResume() {
        super.onResume()
        BooxchangeApp.isInForeground = true

    }

    override fun onPause() {
        super.onPause()
        BooxchangeApp.isInForeground = false
    }
}
