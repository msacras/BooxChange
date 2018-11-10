package nl.booxchange.utilities.recycler

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import nl.booxchange.BR

class GenericBindingViewHolder<T>(val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(model: T?, handler: Any?) {
        binding.setVariable(BR.itemModel, model)
        binding.setVariable(BR.itemHandler, handler)
        binding.executePendingBindings()
    }
}
