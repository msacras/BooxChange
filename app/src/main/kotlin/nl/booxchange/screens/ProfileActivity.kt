package nl.booxchange.screens

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_book_info.*
import nl.booxchange.R
import nl.booxchange.utilities.BaseActivity

class ProfileActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_1)
//        back_button.setOnClickListener { onBackPressed() }
    }
}
