package nl.booxchange.utilities

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.drawable.Animatable
import android.os.Build
import android.support.annotation.LayoutRes
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import nl.booxchange.BR
import nl.booxchange.R
import nl.booxchange.extension.getDrawableCompat
import nl.booxchange.model.Distinctive
import java.lang.ref.WeakReference

class BindingRecyclerAdapter(@LayoutRes private val layoutId: Int, private val handler: Any?): RecyclerView.Adapter<BindingRecyclerAdapter.BindingViewHolder>() {
    inner class BindingViewHolder(val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root)

    private val items = ArrayList<Distinctive>()
    private var recyclerView = WeakReference<RecyclerView>(null)

    fun swapItems(items: Collection<Distinctive>) {
        val recyclerView = recyclerView.get()

        val isInitialScroll = this.items.isEmpty()
        val shouldScrollToStartHorizontally = recyclerView?.computeHorizontalScrollOffset() == 0
        val shouldScrollToEndHorizontally = recyclerView?.computeHorizontalScrollOffset() == recyclerView?.computeHorizontalScrollRange()?.minus(recyclerView.computeHorizontalScrollExtent()).takeIf { it != 0 }
        val shouldScrollToStartVertically = recyclerView?.computeVerticalScrollOffset() == 0
        val shouldScrollToEndVertically = recyclerView?.computeVerticalScrollOffset() == recyclerView?.computeVerticalScrollRange()?.minus(recyclerView.computeVerticalScrollExtent()).takeIf { it != 0 }

        val differences = DiffUtil.calculateDiff(DifferenceResolutionHelper(this.items, items.toList()))
        this.items.clear()
        this.items.addAll(items)
        differences.dispatchUpdatesTo(this)

        if (isInitialScroll) {
            if ((recyclerView?.layoutManager as? LinearLayoutManager)?.stackFromEnd == true) {
                recyclerView.scrollToPosition(items.size - 1)
            } else {
                recyclerView?.scrollToPosition(0)
            }
        } else if (shouldScrollToEndHorizontally || shouldScrollToEndVertically) {
            recyclerView?.scrollToPosition(items.size - 1)
        } else if (shouldScrollToStartHorizontally || shouldScrollToStartVertically) {
            recyclerView?.scrollToPosition(0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        return BindingViewHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), layoutId, parent, false))
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        holder.binding.setVariable(BR.itemModel, items[position])
        holder.binding.setVariable(BR.itemHandler, handler)
    }

    override fun getItemCount() = items.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = WeakReference(recyclerView)
    }

    inner class DifferenceResolutionHelper(private val oldItems: List<Distinctive>, private val newItems: List<Distinctive>): DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]

            val oldItemClass = oldItem::class.java
            val newItemClass = newItem::class.java

            if (oldItemClass != newItemClass) return false

            val idField = oldItemClass.declaredFields.find { it.name == "id" } ?: return false

            idField.isAccessible = true

            return idField.get(oldItem) == idField.get(newItem)
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]

            val itemClass = oldItem::class.java

            return itemClass.declaredFields.all {
                it.isAccessible = true
                it.get(oldItem) == it.get(newItem)
            }
        }

        override fun getOldListSize() = oldItems.size
        override fun getNewListSize() = newItems.size
    }
}
