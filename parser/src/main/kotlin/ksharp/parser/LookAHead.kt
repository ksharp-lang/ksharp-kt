package ksharp.parser

import org.ksharp.common.*
import org.ksharp.common.annotation.Mutable

data class LookAHeadResult<T>(
    val value: T,
    val leaveLastTokens: Int = 0
)

fun <T : Any> Error.asLookAHeadResult(): ErrorOrValue<LookAHeadResult<T>> =
    Either.Left(this)

fun <T> T.asLookAHeadResult(leaveLastTokens: Int = 0): ErrorOrValue<LookAHeadResult<T>> =
    Either.Right(LookAHeadResult(this, leaveLastTokens))

@Mutable
fun <T> Iterator<LexerToken>.lookAHead(block: (Iterator<LexerToken>) -> ErrorOrValue<LookAHeadResult<T>>): ParserResult<T> {
    val collBuilder = listBuilder<LexerToken>()
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
                this.asSequence()
            ).flatten().iterator()
        } else this@lookAHead
        ParserValue(it.value, newIter)
    }.mapLeft {
        val collection = collBuilder.build()
        val newIter = sequenceOf(collection.asSequence(), this.asSequence()).flatten().iterator()
        ParserError(it, newIter)
    }
}
