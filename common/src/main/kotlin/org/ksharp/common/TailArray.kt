package org.ksharp.common

import org.ksharp.common.annotation.Mutable

interface TailArray<T> {
    val size: Int
    fun append(item: T)
    operator fun get(index: Int): T
}

/**
 * This structure maintains the las n elements of an array
 */
@Mutable
private class TailArrayImpl<T>(
    private val capacity: Int
) : TailArray<T> {

    private var position = 0
    private val items = arrayOfNulls<Any?>(capacity)

    override val size: Int get() = position

    override fun append(item: T) {
        items[position % capacity] = item
        position += 1
    }

    @Suppress("UNCHECKED_CAST")
    override operator fun get(index: Int): T =
        if (index >= position) {
            throw IndexOutOfBoundsException("$index >= $position")
        } else if (index < (position - capacity)) {
            throw IndexOutOfBoundsException("$index < ${position - capacity}")
        } else items[index % capacity] as T

}

fun <T> tailArray(capacity: Int): TailArray<T> = TailArrayImpl<T>(capacity)