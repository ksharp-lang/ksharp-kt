package org.ksharp.parser

import org.ksharp.common.*
import org.ksharp.common.annotation.Mutable

data class LookAHeadResult<T, L : LexerValue>(
    val value: T,
    val leaveLastTokens: Int = 0,
    val remainTokens: Iterator<L>
)

fun <T : Any, L : LexerValue> Error.asLookAHeadResult(): ErrorOrValue<LookAHeadResult<T, L>> =
    Either.Left(this)

fun <T, L : LexerValue> T.asLookAHeadResult(
    remainTokens: Iterator<L>,
    leaveLastTokens: Int = 0
): ErrorOrValue<LookAHeadResult<T, L>> =
    Either.Right(LookAHeadResult(this, leaveLastTokens, remainTokens))

@Mutable
fun <T, LV : LexerValue> Iterator<LV>.lookAHead(block: (Iterator<LV>) -> ErrorOrValue<LookAHeadResult<T, LV>>): ParserResult<T, LV> {
    val collBuilder = listBuilder<LV>()
    val seq = generateSequence {
        if (hasNext()) {
            val value = next()
            collBuilder.add(value)
            value
        } else null
    }.iterator()
    val result = block(seq)
    return result.map {
        val newIter = if (it.leaveLastTokens != 0) {
            val collection = collBuilder.build()
            sequenceOf(
                collection.subList((collection.size - it.leaveLastTokens).coerceAtLeast(0), collection.size)
                    .asSequence(),
                it.remainTokens.asSequence()
            ).flatten().iterator()
        } else it.remainTokens
        ParserValue(it.value, newIter)
    }.mapLeft {
        val collection = collBuilder.build()
        val newIter = sequenceOf(collection.asSequence(), this.asSequence()).flatten().iterator()
        ParserError(it, newIter)
    }
}
