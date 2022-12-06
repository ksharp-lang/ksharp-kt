package ksharp.parser

import org.ksharp.common.Either
import org.ksharp.common.Error

data class ParserValue<T>(
    val value: T,
    val remainTokens: Iterator<LexerToken>
)

data class ParserError(
    val error: Error,
    val remainTokens: Iterator<LexerToken>
)

typealias ParserResult<T> = Either<ParserError, ParserValue<T>>

val <T> ParserResult<T>.remainTokens
    get() =
        when (this) {
            is Either.Left -> value.remainTokens
            is Either.Right -> value.remainTokens
        }