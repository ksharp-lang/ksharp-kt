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
