package org.ksharp.common

import java.util.concurrent.atomic.AtomicReference

sealed interface HandlePromise<T> {
    val handle: T?

    fun set(value: T)
}

private class HandlePromiseImpl<T> : HandlePromise<T> {

    private var ref: AtomicReference<T> = AtomicReference(null)

    override val handle: T?
        get() = ref.get()

    override fun set(value: T) {
        if (!ref.compareAndSet(null, value)) throw Exception("Handle already set")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is MockHandlePromise<*>) return true
        if (javaClass != other?.javaClass) return false

        other as HandlePromiseImpl<*>

        return ref == other.ref
    }

    override fun hashCode(): Int {
        return ref.hashCode()
    }


}

class MockHandlePromise<T> : HandlePromise<T> {
    override val handle: T?
        get() = null

    override fun set(value: T) {
        // Mock class
    }
}

fun <T> handlePromise(): HandlePromise<T> = HandlePromiseImpl<T>()
