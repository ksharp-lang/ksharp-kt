package org.ksharp.parser

import org.ksharp.common.*
import org.ksharp.common.annotation.Mutable

data class LookAHeadResult<T, S>(
    val value: T,
    val leaveLastTokens: Int = 0,
    val remainTokens: BaseLexerIterator<S>
)

fun <T : Any, S> ParserResult<T, S>.asLookAHeadResult(): ErrorOrValue<LookAHeadResult<T, S>> =
    when (this) {
        is Either.Left -> value.error.asLookAHeadResult()
        is Either.Right -> value.value.asLookAHeadResult(value.remainTokens)
    }


fun <T : Any, S> Error.asLookAHeadResult(): ErrorOrValue<LookAHeadResult<T, S>> =
    Either.Left(this)

fun <T, S> T.asLookAHeadResult(
    remainTokens: BaseLexerIterator<S>,
    leaveLastTokens: Int = 0
): ErrorOrValue<LookAHeadResult<T, S>> =
    Either.Right(LookAHeadResult(this, leaveLastTokens, remainTokens))

@Mutable
fun <T, S> BaseLexerIterator<S>.lookAHead(block: (BaseLexerIterator<S>) -> ErrorOrValue<LookAHeadResult<T, S>>): ParserResult<T, S> {
    val collBuilder = listBuilder<Token>()
    val seq = generateLexerIterator(state) {
        if (hasNext()) {
            val value = next()
            collBuilder.add(value)
            value
        } else null
    }
    val result = block(seq)
    return result.map {
        val newIter = if (it.leaveLastTokens != 0) {
            val collection = collBuilder.build()
            sequenceOf(
                collection.subList((collection.size - it.leaveLastTokens).coerceAtLeast(0), collection.size)
                    .asSequence(),
                it.remainTokens.asSequence()
            ).flatten().iterator().asLexerIterator(state)
        } else it.remainTokens
        ParserValue(it.value, newIter)
    }.mapLeft {
        val collection = collBuilder.build()
        val newIter = sequenceOf(collection.asSequence(), this.asSequence()).flatten().iterator()
        ParserError(it, listBuilder(), false, newIter.asLexerIterator(state))
    }
}
