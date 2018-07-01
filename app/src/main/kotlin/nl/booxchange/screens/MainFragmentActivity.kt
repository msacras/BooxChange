package nl.booxchange.screens

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageButton
import kotlinx.android.synthetic.main.activity_main_fragment.*
import nl.booxchange.R
import nl.booxchange.R.color.tuna
import nl.booxchange.extension.getColorById
import nl.booxchange.extension.setVisible
import nl.booxchange.extension.toGone
import nl.booxchange.utilities.Constants

class MainFragmentActivity: AppCompatActivity() {

    private val blueColor by lazy { getColorById(R.color.mountainMeadow) }
    private val grayColor by lazy { getColorById(R.color.midGray) }
    private val lightGray by lazy { getColorById(R.color.lightGray) }
    private val colorEvaluator = ArgbEvaluator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_fragment)

        listOf(add_book_button, log_out).forEach { it.setColorFilter(tuna) }

        val fragments = listOf (
                home_page to HomeFragment(),
                message_page to MessageFragment(),
                library_page to LibraryFragment(),
                profile_page to ProfileFragment()
        )

        fragments.forEachIndexed { currentSelectedIndex, (button, fragment) ->
            button.setOnClickListener {
                showFragment(fragment)
                fragments.forEach { (otherButton, _) ->
                    val color = if (otherButton == button) blueColor else grayColor
                    updateColor(otherButton as AppCompatImageButton, color)
                    //app_bar_layout.setExpanded(true, true)
                }
                val targetPositionX = (currentSelectedIndex * focused_button_highlight.width).toFloat()
                val transitionDuration = (Math.abs(focused_button_highlight.translationX - targetPositionX) / (focused_button_highlight.width * 3)) * 450
                focused_button_highlight.animate().translationX(targetPositionX).setDuration(transitionDuration.toLong()).start()
            }
        }

        when (intent.getStringExtra(Constants.EXTRA_PARAM_TARGET_VIEW)) {
            Constants.FRAGMENT_PROFILE -> fragments.first { it.first == profile_page }.first.performClick()
            else -> fragments.first { it.first == home_page }.first.performClick()
        }
    }

    fun setTitle(title: String) {
        toolbar_title.setText(title)
        add_book_button.toGone()
        log_out.toGone()
    }

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

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.fragment, fragment).commit()
    }
}
