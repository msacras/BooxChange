package nl.booxchange.screens

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v4.widget.TextViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageButton
import com.vcristian.combus.expect
import kotlinx.android.synthetic.main.activity_main_fragment.*
import nl.booxchange.R
import nl.booxchange.extension.getColorCompat
import nl.booxchange.extension.setVisible
import nl.booxchange.model.events.ChatsStateChangeEvent
import nl.booxchange.model.events.MessageReceivedEvent
import nl.booxchange.screens.settings.SettingsActivity
import nl.booxchange.utilities.Constants
import org.jetbrains.anko.startActivity

class MainFragmentActivity: AppCompatActivity() {
    val screens by lazy {listOf (
        home_page to "booxchange",
        message_page to "Messages",
        library_page to "Library"
    )}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_fragment)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.title = ""
        toolbar.subtitle = ""

        settings.setOnClickListener {
            startActivity<SettingsActivity>()
        }

        screens.forEach { (button, tag) ->
            button.setOnClickListener {
                showFragment(tag)

                settings.setVisible(tag == "Library")

                fragment_title.text = tag
                TextViewCompat.setTextAppearance(fragment_title, if (tag == "booxchange") R.style.mainPage else R.style.restPage)

                screens.forEach { (otherButton, _) ->
                    updateColor(otherButton as AppCompatImageButton, otherButton == button)
                }
            }
        }

        when (intent.getStringExtra(Constants.EXTRA_PARAM_TARGET_VIEW)) {
//            Constants.FRAGMENT_PROFILE -> profile_page.performClick()
//            Constants.FRAGMENT_CHAT -> post(ChatOpenedEvent(chatId = intent.getStringExtra(Constants.EXTRA_PARAM_CHAT_ID)))
            else -> home_page.performClick()
        }

        expect(MessageReceivedEvent::class.java) { (messageModel) ->
//                        if (messageModel.id == UserData.Session.id) return@expect
            //TODO: unread messages counter icon

            messageModel.content
        }

        expect(ChatsStateChangeEvent::class.java) { (count) ->

        }
    }

    private val greenColor by lazy { getColorCompat(R.color.springGreen) }
    private val whiteColor by lazy { getColorCompat(R.color.whiteGray) }
    private val darkGreenColor by lazy { getColorCompat(R.color.darkGreen) }
    private val colorEvaluator = ArgbEvaluator()

    private fun updateColor(button: AppCompatImageButton, isActiveButton: Boolean) {
        val (fromButtonColor, fromIconColor) = (button.tag as? Map<String, Int>)?.run {
            get("button_color")!! to get("icon_color")!!
        } ?: run {
            whiteColor to darkGreenColor
        }

        val (toButtonColor, toIconColor) = if (isActiveButton) {
            greenColor to whiteColor
        } else {
            whiteColor to darkGreenColor
        }

        ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                val currentButtonColor = colorEvaluator.evaluate(it.animatedFraction, fromButtonColor, toButtonColor) as Int
                val currentIconColor = colorEvaluator.evaluate(it.animatedFraction, fromIconColor, toIconColor) as Int

                button.setBackgroundColor(currentButtonColor)
                button.setColorFilter(currentIconColor)

                button.tag = mapOf(
                    "button_color" to currentButtonColor,
                    "icon_color" to currentIconColor
                )
            }
            duration = 150
            start()
        }
    }

    @SuppressLint("CommitTransaction")
    fun showFragment(tag: String) {
        supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).apply {
            supportFragmentManager.fragments.forEach { fragment ->
                if (fragment.tag == tag) show(fragment) else hide(fragment)
            }
            commit()
        }
    }
}
