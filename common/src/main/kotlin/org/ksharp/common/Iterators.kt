package org.ksharp.common

inline fun <T> generateIterator(crossinline generator: () -> T?): Iterator<T> = object : Iterator<T> {
    private var current: T? = null

    override fun hasNext(): Boolean {
        if (current != null) return true
        current = generator()
        return current != null
    }

    override fun next(): T {
        if (current == null) {
            throw NoSuchElementException()
        }
        val result = current!!
        current = null
        return result
    }
}
