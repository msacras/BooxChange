package nl.booxchange.screens.home

import android.arch.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class FirebaseItemQueryLiveData(private val query: Query): LiveData<Pair<String, Map<String, Any>>>() {
    private val valueListener = object: ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError) {}
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            value = dataSnapshot.key!! to dataSnapshot.value as Map<String, Any>
        }
    }

    override fun onActive() {
        super.onActive()
        query.addValueEventListener(valueListener)
    }

    override fun onInactive() {
        super.onInactive()
        query.removeEventListener(valueListener)
    }
}
