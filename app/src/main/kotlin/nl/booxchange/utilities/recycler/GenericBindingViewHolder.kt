package nl.booxchange.utilities.recycler

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import nl.booxchange.BR

class GenericBindingViewHolder<T>(val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(model: T?, handler: Any?) {
        binding.setVariable(BR.itemModel, model)
        binding.setVariable(BR.itemHandler, handler)
    }
}
