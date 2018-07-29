package nl.booxchange.utilities

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import nl.booxchange.BR
import nl.booxchange.model.Distinctive

class LiveBindingPagedListAdapter(@LayoutRes private val layoutId: Int, private val handler: Any?): PagedListAdapter<Distinctive, LiveBindingPagedListAdapter.GenericBindingViewHolder>(itemDifferenceResolutionHelper) {
    inner class GenericBindingViewHolder(val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(model: Distinctive) {
            binding.setVariable(BR.itemModel, model)
            binding.setVariable(BR.itemHandler, handler)
        }
    }

    private val itemsObserver = Observer<PagedList<Distinctive>>(::submitList)
    private var observableList: LiveData<PagedList<Distinctive>>? = null

    fun observeList(itemsList: LiveData<PagedList<Distinctive>>) {
        observableList?.removeObserver(itemsObserver)
        observableList = itemsList
        observableList?.observeForever(itemsObserver)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericBindingViewHolder {
        return GenericBindingViewHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), layoutId, parent, false))
    }

    override fun onBindViewHolder(holder: GenericBindingViewHolder, position: Int) {
        holder.bind(currentList!![position]!!)
    }

    companion object {
        val itemDifferenceResolutionHelper = object: DiffUtil.ItemCallback<Distinctive>() {
            override fun areItemsTheSame(oldItem: Distinctive?, newItem: Distinctive?): Boolean {
                return oldItem?.id == newItem?.id
            }

            override fun areContentsTheSame(oldItem: Distinctive?, newItem: Distinctive?): Boolean {
                return false
            }
        }
    }
}
