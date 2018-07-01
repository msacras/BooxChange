package nl.booxchange.screens.chat

import android.databinding.ObservableField

class ObservableMutableSet<T>: ObservableField<MutableSet<T>>() {
    init {
        set(mutableSetOf())
    }

    fun add(element: T): Boolean = get().add(element).also { notifyChange() }

    fun addAll(elements: Collection<T>): Boolean = get().addAll(elements).also { notifyChange() }

    fun clear() = get().clear().also { notifyChange() }

    fun iterator(): MutableIterator<T> = get().iterator()

    fun remove(element: T): Boolean = get().remove(element).also { notifyChange() }

    fun removeAll(elements: Collection<T>): Boolean = get().removeAll(elements).also { notifyChange() }

    fun retainAll(elements: Collection<T>): Boolean = get().retainAll(elements).also { notifyChange() }

    val size = get().size

    fun contains(element: T): Boolean = get().contains(element)

    fun containsAll(elements: Collection<T>): Boolean = get().containsAll(elements)

    fun isEmpty(): Boolean = get().isEmpty()

    override fun set(value: MutableSet<T>) = super.set(value)

    override fun get(): MutableSet<T> = super.get()!!
}
