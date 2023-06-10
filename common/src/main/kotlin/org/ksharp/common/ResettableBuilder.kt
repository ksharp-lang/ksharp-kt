package org.ksharp.common

import org.ksharp.common.annotation.Mutable

typealias ResettableListBuilder<Item> = ResettableBuilder<MutableList<Item>, List<Item>, ListBuilder<Item>>

@Mutable
class ResettableBuilder<S, V, T : Builder<S, V>>(private val initializer: () -> T) {

    private var value: T? = null

    fun build(): V? {
        if (value != null) {
            val result = value!!.build()
            value = null
            return result
        }
        return null
    }

    fun update(update: (current: T) -> Unit) {
        if (value == null) {
            value = initializer()
        }
        update(value!!)
    }

}

fun <Item> resettableListBuilder() = ResettableListBuilder<Item> {
    listBuilder()
}

@Mutable
class ResettableValue<V> {

    private var value: V? = null

    fun get(): V? {
        if (value != null) {
            val result = value!!
            value = null
            return result
        }
        return null
    }

    fun set(value: V) {
        this.value = value
    }

    fun reset() {
        this.value = null
    }

}

fun <Item> resettableValue() = ResettableValue<Item>()
