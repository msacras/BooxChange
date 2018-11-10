package nl.booxchange.utilities.database

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import nl.booxchange.model.FirestoreObject

class BooksPagingDataSource<T: FirestoreObject>(var baseQuery: Query, private val objectTransformer: (DocumentSnapshot) -> T) {
    private inner class SingleValueEventListener(private val onSuccess: (List<T>) -> Unit, private val onFailure: () -> Unit = { onSuccess(emptyList()) }): OnCompleteListener<QuerySnapshot> {
        override fun onComplete(queryTask: Task<QuerySnapshot>) {
            queryTask.result?.documents?.map(objectTransformer)?.let(onSuccess) ?: onFailure()
        }
    }

    var filteringString = ""

    fun loadNext(fromDocument: DocumentSnapshot?, callback: (List<T>) -> Unit) {
        baseQuery
            .orderBy("timestamp", Query.Direction.DESCENDING)
//            .run { fromKey?.run { startAfter(fromKey) } ?: this }
            .run { fromDocument?.run { startAfter(fromDocument) } ?: this }
            .run { if (filteringString.length > 2) whereArrayContains("searchIndices", filteringString.toLowerCase()) else this }
            .limit(REQUEST_LOAD_SIZE)
            .get()
            .addOnCompleteListener(SingleValueEventListener(callback))
    }

    companion object {
        const val REQUEST_LOAD_SIZE = 8L
    }
}
