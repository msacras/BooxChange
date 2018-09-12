package nl.booxchange.utilities.database

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import nl.booxchange.model.FirebaseObject

class FirebasePagingDataSource<T: FirebaseObject>(var baseQuery: Query, private val objectTransformer: (Pair<String, Map<String, Any>>) -> T) {
    private inner class SingleValueEventListener(private val onSuccess: (List<T>) -> Unit, private val onFailure: () -> Unit = { onSuccess(emptyList()) }): ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError) {
            onFailure()
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            dataSnapshot.children.mapNotNull { snapshot ->
                snapshot.key!! to snapshot.value as Map<String, Any>
            }.map(objectTransformer).let(onSuccess)
        }
    }

    fun loadInitial(callback: (List<T>) -> Unit) {
        baseQuery.orderByKey().limitToLast(REQUEST_LOAD_SIZE).addListenerForSingleValueEvent(SingleValueEventListener(callback))
    }

    fun loadAfter(fromKey: String, callback: (List<T>) -> Unit) {
        baseQuery.orderByKey().startAt(fromKey).limitToFirst(REQUEST_LOAD_SIZE).addListenerForSingleValueEvent(SingleValueEventListener(onSuccess = {
            callback(it.drop(1))
        }))
    }

    fun loadBefore(toKey: String, callback: (List<T>) -> Unit) {
        baseQuery.orderByKey().endAt(toKey).limitToLast(REQUEST_LOAD_SIZE).addListenerForSingleValueEvent(SingleValueEventListener(onSuccess = {
            callback(it.dropLast(1))
        }))
    }

    fun getKey(item: T): String {
        return item.id
    }

    companion object {
        const val REQUEST_LOAD_SIZE = 15
    }
}
