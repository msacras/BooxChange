package nl.booxchange.utilities.recycler

import android.support.annotation.LayoutRes

data class ViewHolderConfig<T>(@LayoutRes val layoutId: Int, val viewType: ViewType, val matching: (Int, T?) -> Boolean = { _, _ -> true }) {
    enum class ViewType {
        PLACEHOLDER, BOOK, MESSAGE_TEXT, MESSAGE_IMAGE, MESSAGE_REQUEST, MESSAGE_MEETING, CHAT_REQUEST, CHAT_ACTIVE
    }
}
