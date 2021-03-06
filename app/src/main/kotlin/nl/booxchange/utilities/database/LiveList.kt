package nl.booxchange.utilities.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import nl.booxchange.model.FirestoreObject

class LiveList<T: Any>: MutableLiveData<List<T?>>() {
    fun initWithPlaceholders(size: Int = 15) {
        value = List(size) { null }
    }

    override fun setValue(value: List<T?>?) {
        super.setValue(value.orEmpty())
    }

    override fun getValue(): List<T?> {
        return super.getValue().orEmpty()
    }

    override fun postValue(value: List<T?>?) {
        super.postValue(value.orEmpty())
    }

    operator fun plusAssign(value: List<T>) {
        setValue(getValue() + value)
    }

    operator fun minusAssign(value: T) {
        setValue((getValue() as MutableList).apply {
            remove(value)
        })
    }

    operator fun set(key: String, value: T) {
        setValue((getValue() as MutableList).apply {
            find { (it as? FirestoreObject)?.id == key }
            ?.let(::indexOf)
            ?.let { index -> set(index, value) }
        })
    }

    fun prependItems(value: List<T>) {
        setValue(value + getValue())
    }

    fun isEmpty(): Boolean {
        return value.isEmpty()
    }
}
