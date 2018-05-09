package nl.booxchange.screens

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import kotlinx.android.synthetic.main.activity_main_fragment.*
import nl.booxchange.R

class MainFragmentActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_fragment)

        val fragments = listOf (
                home_page to HomeFragment(),
                message_page to MessageFragment(),
                library_page to LibraryFragment(),
                profile_page to ProfileFragment()
        )

        showFragment(fragments.first().second)

        fragments.forEachIndexed { index, (button, fragment) ->
            button.setOnClickListener {
                showFragment(fragment)
                focused_button_highlight.animate().translationX((index * focused_button_highlight.width).toFloat()).setDuration(350).start()
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment).commit()
    }
}
