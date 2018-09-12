package nl.booxchange.utilities.recycler

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import java.lang.ref.WeakReference


@Suppress("UNCHECKED_CAST")
class LiveBindingListAdapter<T>(private val viewConfigs: List<ViewHolderConfig<T>>, private val handler: Any?): RecyclerView.Adapter<GenericBindingViewHolder<T>>() {

    private val itemsUpdater = AsyncListDiffer(this, ItemDifferenceResolutionHelper)
    private val itemsObserver = Observer<List<T>>(itemsUpdater::submitList)
    private var observableList: LiveData<List<T>>? = null

    private var recyclerViewReference = WeakReference<RecyclerView>(null)

    fun observeList(itemsList: LiveData<List<T>>) {
        observableList?.removeObserver(itemsObserver)
        observableList = itemsList
        observableList?.observeForever(itemsObserver)
    }

    init {
        registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerViewReference.get()?.run {
                    postDelayed({
                        val smoothScroller = object: LinearSmoothScroller(context) {
                            init {
                                setTargetPosition((positionStart + itemCount - 1).coerceAtLeast(0))
                            }

                            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                                return super.calculateSpeedPerPixel(displayMetrics) * 8f
                            }

                            override fun getVerticalSnapPreference(): Int {
                                return LinearSmoothScroller.SNAP_TO_START
                            }
                        }

                        (layoutManager as LinearLayoutManager).startSmoothScroll(smoothScroller)
                    }, 250)
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericBindingViewHolder<T> {
        val viewType = ViewHolderConfig.ViewType.values()[viewType]
        return GenericBindingViewHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), viewConfigs.first { it.viewType == viewType }.layoutId, parent, false))
    }

    override fun onBindViewHolder(holder: GenericBindingViewHolder<T>, position: Int) {
        holder.bind(getItem(position), handler)
    }

    override fun getItemViewType(position: Int): Int {
        return viewConfigs.first { it.matching(position, getItem(position)) }.viewType.ordinal
    }

    override fun getItemCount(): Int {
        return itemsUpdater.currentList.size
    }

    private fun getItem(position: Int): T? {
        return itemsUpdater.currentList[position] as? T
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerViewReference = WeakReference(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerViewReference = WeakReference<RecyclerView>(null)
    }

    companion object {
        object ItemDifferenceResolutionHelper: DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return oldItem::class.java.declaredFields.find { it.name == "id" }?.run {
                    isAccessible = true
                    get(oldItem) == get(newItem)
                } ?: false
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return oldItem::class.java.declaredFields.all { field ->
                    field.isAccessible = true
                    field.get(oldItem) == field.get(newItem)
                }
            }
        }
    }
}
