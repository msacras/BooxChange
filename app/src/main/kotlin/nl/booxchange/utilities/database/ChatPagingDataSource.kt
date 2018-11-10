package nl.booxchange.utilities.database

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import nl.booxchange.model.FirestoreObject

class ChatPagingDataSource<T: FirestoreObject>(var baseQuery: CollectionReference, private val objectTransformer: (DocumentSnapshot) -> T) {
    private inner class SingleValueEventListener(private val onSuccess: (List<T>) -> Unit, private val onFailure: () -> Unit = { onSuccess(emptyList()) }): OnCompleteListener<QuerySnapshot> {
        override fun onComplete(queryTask: Task<QuerySnapshot>) {
            queryTask.result?.documents?.map(objectTransformer)?.let(onSuccess) ?: onFailure()
        }
    }

    fun loadInitial(callback: (List<T>) -> Unit) {
        baseQuery.orderBy("timestamp", Query.Direction.DESCENDING).limit(REQUEST_LOAD_SIZE).get().addOnCompleteListener(SingleValueEventListener({callback(it.reversed())}))
    }

    fun loadAfter(fromKey: Long, callback: (List<T>) -> Unit) {
        baseQuery.orderBy("timestamp").startAfter(fromKey).limit(REQUEST_LOAD_SIZE).get().addOnCompleteListener(SingleValueEventListener(callback))
    }

    fun loadBefore(toKey: Long, callback: (List<T>) -> Unit) {
        baseQuery.orderBy("timestamp", Query.Direction.DESCENDING).startAfter(toKey).limit(REQUEST_LOAD_SIZE).get().addOnCompleteListener(SingleValueEventListener({callback(it.reversed())}))
    }

    companion object {
        const val REQUEST_LOAD_SIZE = 15L
    }
}
