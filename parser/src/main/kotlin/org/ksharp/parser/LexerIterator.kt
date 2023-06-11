package org.ksharp.parser

import org.ksharp.common.annotation.Mutable

@Mutable
open class LexerState<V>(initialValue: V) {
    var value = initialValue
        private set

    fun update(newValue: V) {
        value = newValue
    }
}

interface LexerIterator<out T : Token, V> {
    val state: LexerState<V>
    fun hasNext(): Boolean
    fun next(): T
}

fun <T : Token, V> LexerIterator<T, V>.asSequence() = generateSequence { if (hasNext()) next() else null }

fun <T : Token, S> LexerIterator<T, S>.filter(predicate: (T) -> Boolean) = generateLexerIterator(state) {
    while (hasNext()) {
        val item = next()
        if (predicate(item)) return@generateLexerIterator item
    }
    null
}

fun <T : Token, S> Iterator<T>.asLexerIterator(state: LexerState<S>) = generateLexerIterator(state) {
    if (hasNext()) next()
    else null
}


inline fun <T : Token, V> generateLexerIterator(
    state: LexerState<V>,
    crossinline generator: () -> T?
): LexerIterator<T, V> {
    return object : LexerIterator<T, V> {
        private var current: T? = null
        override val state: LexerState<V> = state

        override fun hasNext(): Boolean {
            current = generator()
            return current != null
        }

        override fun next(): T = current ?: throw NoSuchElementException()
    }
}

fun <T : Token, S> emptyLexerIterator(state: LexerState<S>): LexerIterator<T, S> = generateLexerIterator(state) {
    null
}

internal class ConsLexerIterator<T : Token, V> internal constructor(
    private val token: T,
    internal val iterator: LexerIterator<T, V>
) : LexerIterator<T, V> {
    internal var tokenConsumed = false
        private set

    override val state: LexerState<V> = iterator.state

    override fun hasNext(): Boolean {
        if (!tokenConsumed) {
            return true
        }
        return iterator.hasNext()
    }

    override fun next(): T {
        if (!tokenConsumed) {
            tokenConsumed = true
            return token
        }
        return iterator.next()
    }

}

fun <T : Token, V> LexerIterator<T, V>.cons(token: T): LexerIterator<T, V> {
    if (this is ConsLexerIterator && tokenConsumed) {
        return ConsLexerIterator(token, iterator)
    }
    return ConsLexerIterator(token, this)
}
