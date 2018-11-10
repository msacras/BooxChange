package nl.booxchange.utilities.database

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query

class FirestoreListQueryLiveData(query: Query): LiveData<List<DocumentSnapshot>>() {
    init {
        query.addSnapshotListener { querySnapshot, _ ->
            value = querySnapshot?.documents.orEmpty()
        }
    }
}
