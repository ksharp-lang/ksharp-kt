package org.ksharp.parser

import org.ksharp.common.annotation.Mutable

@Mutable
open class LexerState<V>(
    initialValue: V,
    val lookAHeadState: LookAheadLexerState = LookAheadLexerState()
) {
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


class LookAheadLexerIterator<T : Token, V>(private val lexer: LexerIterator<T, V>) : LexerIterator<T, V> by lexer {

    private var bufferSize = 0

    fun inCheckpoint(nextToken: LookAheadLexerIterator<T, V>.() -> T?): T? {
        val checkpoint = state.lookAHeadState.checkpoint()
        val result = nextToken()
        if (bufferSize != 0) {
            checkpoint.end(bufferSize)
            clearBuffer()
        } else checkpoint.end(bufferSize)
        return result
    }

    fun lookNext(): T {
        val result = next()
        bufferSize += 1
        return result
    }

    fun clearBuffer() {
        bufferSize = 0
    }
}

fun <T : Token, V> LexerIterator<T, V>.asSequence() = generateSequence { if (hasNext()) next() else null }

fun <T : Token, S> Iterator<T>.asLexerIterator(state: LexerState<S>) = generateLexerIterator(state) {
    if (hasNext()) next()
    else null
}

inline fun <T : Token, V> generateLexerIterator(
    state: LexerState<V>,
    crossinline generator: () -> T?
): LexerIterator<T, V> =
    object : LexerIterator<T, V> {
        private var current: T? = null
        override val state: LexerState<V> = state

        override fun hasNext(): Boolean {
            current = generator()
            return current != null
        }

        override fun next(): T = current ?: throw NoSuchElementException()
    }

fun <T : Token, S> emptyLexerIterator(state: LexerState<S>): LexerIterator<T, S> = generateLexerIterator(state) {
    null
}

fun <T : Token, S> oneLexerIterator(state: LexerState<S>, value: T): LexerIterator<T, S> {
    var result: T? = value
    return generateLexerIterator(state) {
        result.also { result = null }
    }
}

fun <T : Token, S> LexerIterator<T, S>.filter(predicate: (T) -> Boolean) = generateLexerIterator(state) {
    while (hasNext()) {
        val item = next()
        if (predicate(item)) return@generateLexerIterator item
    }
    null
}

fun <I : Token, O : Token, S> LexerIterator<I, S>.map(transform: (value: I) -> O): LexerIterator<O, S> =
    generateLexerIterator(state) {
        if (hasNext()) {
            transform(next())
        } else null
    }

fun <T : Token, S> LexerIterator<T, S>.generateIteratorWithLookAhead(nextValue: LookAheadLexerIterator<T, S>.() -> T?): LexerIterator<T, S> {
    val lexer = LookAheadLexerIterator(this)
    return generateLexerIterator(state) {
        lexer.inCheckpoint(nextValue)
    }
}
