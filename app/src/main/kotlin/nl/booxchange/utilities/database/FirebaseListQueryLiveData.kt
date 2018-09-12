package nl.booxchange.utilities.database

import android.arch.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class FirebaseListQueryLiveData(private val query: Query): LiveData<Map<String, Map<String, Any>>>() {
    private val valueListener = object: ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError) {}
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            value = dataSnapshot.children.mapNotNull { it.key!! to it.value as Map<String, Any> }.toMap()
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
