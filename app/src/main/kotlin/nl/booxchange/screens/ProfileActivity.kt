package nl.booxchange.screens

import android.os.Bundle
import nl.booxchange.R
import nl.booxchange.utilities.NavigationActivity

class ProfileActivity : NavigationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_1)
//        back_button.setOnClickListener { onBackPressed() }
    }
}
