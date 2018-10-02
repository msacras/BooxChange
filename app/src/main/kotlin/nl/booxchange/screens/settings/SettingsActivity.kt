package nl.booxchange.screens.settings

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_settings.*
import nl.booxchange.BuildConfig
import nl.booxchange.R
import nl.booxchange.widget.BottomSheetDialog
import nl.booxchange.screens.SignInActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        back.setOnClickListener { onBackPressed() }
        sign_out.setOnClickListener {
            logOut()
        }

        val userName = FirebaseAuth.getInstance().currentUser
        val dbRef = FirebaseDatabase.getInstance().getReference("users")

        dbRef.child(userName!!.uid).addListenerForSingleValueEvent (object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.child("last_name").value.toString()
                user_name.setText(name)
            }
        })

        //user_name.text = dbRef.child(userModel.get()?.last_name).setValue().toString()

        val bottomSheetDialog = BottomSheetDialog()
        version.text = ("v." + BuildConfig.VERSION_NAME)
        val user = FirebaseAuth.getInstance().currentUser?.photoUrl
        Glide.with(this).load(user).apply(RequestOptions().circleCrop()).into(profile_image)
        profile_image.setOnClickListener {
            bottomSheetDialog.show(supportFragmentManager, null)
        }
    }

    override fun onBackPressed() {
        this.finish()
    }

    private fun logOut() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finishAffinity()
        LoginManager.getInstance().logOut()
        FirebaseAuth.getInstance().signOut()
    }
}
