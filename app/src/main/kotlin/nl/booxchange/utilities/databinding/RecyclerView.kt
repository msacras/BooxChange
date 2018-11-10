package nl.booxchange.utilities.databinding

import androidx.lifecycle.LiveData
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import nl.booxchange.model.entities.BookModel
import nl.booxchange.utilities.database.LiveList
import nl.booxchange.utilities.recycler.LiveBindingListAdapter
import nl.booxchange.utilities.recycler.ViewHolderConfig

@BindingAdapter("listLayout", "listHandler", "listItems")
fun <T: Any> RecyclerView.setupLiveListRecyclerAdapter(viewConfigs: List<ViewHolderConfig<T>>?, withHandler: Any?, itemsList: LiveData<List<T>>?) {
    if (adapter != null || itemsList == null || viewConfigs == null) return

    adapter = LiveBindingListAdapter(viewConfigs, withHandler).apply { observeList(itemsList) }
}

@BindingAdapter("recyclerLayout", "recyclerHandler", "recyclerItems")
fun <T: Any> RecyclerView.setupLiveRecyclerAdapter(viewConfigs: List<ViewHolderConfig<T>>?, withHandler: Any?, itemsList: LiveData<List<T>>?) {
    if (adapter != null || itemsList == null || viewConfigs == null) return

    adapter = LiveBindingListAdapter(viewConfigs, withHandler).apply { observeList(itemsList) }
}
