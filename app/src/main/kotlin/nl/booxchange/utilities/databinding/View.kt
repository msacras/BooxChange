package nl.booxchange.utilities.databinding

import android.databinding.BindingAdapter
import android.view.View
import nl.booxchange.extension.setVisible


@BindingAdapter("visibleIf")
fun View.setViewVisibility(isVisible: Boolean) {
    setVisible(isVisible)
}

