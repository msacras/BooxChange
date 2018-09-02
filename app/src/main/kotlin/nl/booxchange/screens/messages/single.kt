package nl.booxchange.screens.messages

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

fun Query.single(completion: (Pair<String, Map<String, Any>>?) -> Unit) {
    addListenerForSingleValueEvent(object: ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError) {
            databaseError.toException().printStackTrace()
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            completion((dataSnapshot.value as? Map<String, Any>)?.let { dataSnapshot.key!! to it })
        }
    })
}
