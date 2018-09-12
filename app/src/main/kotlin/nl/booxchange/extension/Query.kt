package nl.booxchange.extension

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import org.jetbrains.anko.doAsyncResult

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

val Query.value: Any? get() {
    var completed = false
    var result: Any? = null

    addListenerForSingleValueEvent(object: ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError) {
            databaseError.toException().printStackTrace()
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            result = dataSnapshot.value
            completed = true
        }
    })

    val synchronizer = doAsyncResult {
        while (!completed) {

        }

        return@doAsyncResult result
    }

    return synchronizer.get()
}

fun Query.collection(completion: (Map<String, Map<String, Any>>?) -> Unit) {
    addListenerForSingleValueEvent(object: ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError) {
            databaseError.toException().printStackTrace()
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val result = dataSnapshot.children.map {
                dataSnapshot.key!! to dataSnapshot.value as Map<String, Any>
            }.toMap()

            completion(result)
        }
    })
}
