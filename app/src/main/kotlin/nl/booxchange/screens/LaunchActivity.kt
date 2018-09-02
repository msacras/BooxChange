package nl.booxchange.screens

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * Created by Cristian Velinciuc on 3/12/18.
 */
class LaunchActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = if (FirebaseAuth.getInstance().currentUser != null) MainFragmentActivity::class.java else SignInActivity::class.java

        startActivity(Intent(this, activity).addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT))
        finish()
    }
}
