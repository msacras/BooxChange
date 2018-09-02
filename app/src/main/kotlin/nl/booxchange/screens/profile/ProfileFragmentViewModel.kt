package nl.booxchange.screens.profile

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import nl.booxchange.screens.SignInActivity
import nl.booxchange.screens.library.UserModel
import nl.booxchange.utilities.BaseViewModel
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class ProfileFragmentViewModel: BaseViewModel() {
    val userName = ObservableField<String>()
    val userPhoto = ObservableField<String>()
    val userBooks = ObservableInt()

    init {
        FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().currentUser?.uid!!).addValueEventListener(object: ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userModel = (dataSnapshot.value as? Map<String, Any>)?.let { UserModel.fromFirebaseEntry(dataSnapshot.key!! to it) }

                userName.set(userModel?.alias)
                userPhoto.set(userModel?.image)
            }
        })
    }

    fun View.deleteProfile() {
        FirebaseAuth.getInstance().currentUser?.delete()?.addOnCompleteListener {
            if (it.isSuccessful) {
                context.startActivity<SignInActivity>()
                (context as AppCompatActivity).finish()
            } else {
                context.toast(it.exception?.localizedMessage ?: "unknown exception")
            }
        }
    }
}
