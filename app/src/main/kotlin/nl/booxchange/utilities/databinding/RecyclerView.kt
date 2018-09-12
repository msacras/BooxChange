package nl.booxchange.utilities.databinding

import android.arch.lifecycle.LiveData
import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import nl.booxchange.utilities.recycler.LiveBindingListAdapter
import nl.booxchange.utilities.recycler.ViewHolderConfig

@BindingAdapter("recyclerLayout", "recyclerHandler", "recyclerItems")
fun <T: Any> RecyclerView.setupLiveRecyclerAdapter(viewConfigs: List<ViewHolderConfig<T>>?, withHandler: Any?, itemsList: LiveData<List<T>>?) {
    if (adapter != null || itemsList == null || viewConfigs == null) return

    adapter = LiveBindingListAdapter(viewConfigs, withHandler).apply { observeList(itemsList) }
}
