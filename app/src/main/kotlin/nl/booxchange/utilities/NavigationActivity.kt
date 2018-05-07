package nl.booxchange.utilities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.annotation.StringRes
import kotlinx.android.synthetic.main.drawer_layout.view.*
import nl.booxchange.R
import nl.booxchange.screens.*
import nl.booxchange.widget.SlidingNavigationLayout
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.withAlpha

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
open class NavigationActivity : BaseActivity() {
    private val rootLayout by lazy { SlidingNavigationLayout(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(rootLayout)
        initializeDrawerLayout()
    }

    override fun setContentView(layoutResID: Int) {
        setRootView(rootLayout.setContentView(layoutResID))
    }

    private fun initializeDrawerLayout() {
        rootLayout.setDrawerView(R.layout.drawer_layout).let { view ->
            val activityButtons = listOf(
                view.go_to_homepage_button to HomepageActivity::class,
                view.go_to_profile_button to ProfileActivity::class,
                view.go_to_messages_button to MessagesActivity::class,
                view.go_to_library_button to LibraryActivity::class
            )

            activityButtons.forEach { (view, activityClass) ->
                if (this::class == activityClass) {
                    view.background = ColorDrawable(Color.WHITE.withAlpha(100)) //TODO: fix this shitty highlighting
                }
                view.setOnClickListener {
                    if (this::class != activityClass) {
                        rootLayout.setDrawerOpen(false)
                        view.postDelayed({
                            if (activityClass != HomepageActivity::class) {
                                startActivity(Intent(this, activityClass.java))
                            }
                            if (this !is HomepageActivity) {
                                finish()
                            }
                        }, 220)
                    }
                }
            }

            view.go_to_logout_button.setOnClickListener {
                UserData.Authentication.logout()
                startActivity<LaunchActivity>()
                finish()
            }
        }
    }

    protected fun showSnackbar(@StringRes message: Int) {
        rootLayout.showSnackbar(message)
    }

    override fun onBackPressed() {
        if (rootLayout.isDrawerOpen) {
            rootLayout.setDrawerOpen(false)
        } else {
            super.onBackPressed()
        }
    }
}
