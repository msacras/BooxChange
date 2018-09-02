package nl.booxchange.utilities

import android.support.annotation.LayoutRes

data class ViewHolderConfig<T>(@LayoutRes val layoutId: Int, val viewType: Int, val matching: (Int, T) -> Boolean)
