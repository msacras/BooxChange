package com.example.dima.booxchange.utilities

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dima.booxchange.model.Distinctive
import java.lang.ref.WeakReference

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
open class RecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  inner class RecyclerViewViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
  inner class ViewModelPair<T: Distinctive>(@LayoutRes val layoutId: Int, val dataModelClass: Class<T>, val bindingFunction: (view: View, model: T) -> Unit, val viewTypeCondition: (position: Int, model: T) -> Boolean)

  private val viewModelPairsList = ArrayList<ViewModelPair<Distinctive>>()
  private val items = mutableListOf<Distinctive>()
  private var recyclerView: WeakReference<RecyclerView>? = null

  fun <T: Distinctive> addModelToViewBinding(@LayoutRes layoutId: Int, dataModelClass: Class<T>, bindingFunction: (view: View, model: T) -> Unit) {
    addModelToViewBinding(layoutId, dataModelClass, { _, _ -> true }, bindingFunction)
  }

  fun <T: Distinctive> addModelToViewBinding(@LayoutRes layoutId: Int, dataModelClass: Class<T>, viewTypeCondition: (position: Int, model: T) -> Boolean, bindingFunction: (view: View, model: T) -> Unit) {
    viewModelPairsList.add(ViewModelPair(layoutId, dataModelClass, bindingFunction, viewTypeCondition) as ViewModelPair<Distinctive>)
  }

  fun swapItems(newItems: List<Distinctive>) {
    items.clear()
    items.addAll(newItems)
    notifyDataSetChanged()
  }

  fun appendItems(newItems: List<Distinctive>) {
    val insertPosition = items.size
    val newList = newItems.filter { !items.contains(it) }
    val insertedCount = newList.size
    items.addAll(newList)
    if (insertPosition == 0) {
      notifyDataSetChanged()
    } else {
      notifyItemRangeInserted(insertPosition, insertedCount)
    }
  }

  fun prependItems(newItems: List<Distinctive>) {
    val newList = newItems.filter { !items.contains(it) }
    items.addAll(0, newList)
    notifyItemRangeInserted(0, newList.size)
    recyclerView?.get()?.scrollToPosition(0)
  }

  fun clearItems() {
    val itemsCount = items.size
    items.clear()
    notifyItemRangeRemoved(0, itemsCount)
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    this.recyclerView = WeakReference(recyclerView)
  }

  override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
    super.onDetachedFromRecyclerView(recyclerView)
    this.recyclerView = null
  }

  override fun getItemCount(): Int {
    return items.size
  }

  override fun getItemViewType(position: Int): Int {
    return viewModelPairsList.indexOfFirst { it.viewTypeCondition(position, items[position % items.size]) && items[position % items.size]::class.java == it.dataModelClass }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewViewHolder {
    return RecyclerViewViewHolder(LayoutInflater.from(parent.context).inflate(viewModelPairsList[viewType].layoutId, parent, false))
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    viewModelPairsList[getItemViewType(position)].bindingFunction(holder.itemView, items[position % items.size])
  }
}
