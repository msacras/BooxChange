package com.example.dima.booxchange.utilities

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
open class RecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  inner class RecyclerViewViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
  inner class ViewModelPair<T: Any>(@LayoutRes val layoutId: Int, val dataModelClass: Class<T>, val bindingFunction: (view: View, model: Any) -> Unit, val viewTypeCondition: (position: Int, model: Any) -> Boolean)

  private val viewModelPairsList = ArrayList<ViewModelPair<Any>>()
  private val items = mutableListOf<Any>()

  fun <T: Any> addModelToViewBinding(@LayoutRes layoutId: Int, dataModelClass: Class<T>, bindingFunction: (view: View, model: T) -> Unit) {
    addModelToViewBinding(layoutId, dataModelClass, { _, _ -> true }, bindingFunction)
  }

  fun <T: Any> addModelToViewBinding(@LayoutRes layoutId: Int, dataModelClass: Class<T>, viewTypeCondition: (position: Int, model: T) -> Boolean, bindingFunction: (view: View, model: T) -> Unit) {
    viewModelPairsList.add(ViewModelPair(layoutId, dataModelClass, bindingFunction as (View, Any) -> Unit, viewTypeCondition as (Int, Any) -> Boolean) as ViewModelPair<Any>)
  }

  fun addItems(newItems: List<Any>) {
    items.addAll(newItems)
    notifyDataSetChanged()
  }

  fun swapItems(newItems: List<Any>) {
    items.clear()
    items.addAll(newItems)
    notifyDataSetChanged()
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
