package ksharp.parser

import org.ksharp.common.*
import org.ksharp.common.annotation.Mutable

enum class BaseParserErrorCode(override val description: String) : ErrorCode {
    EofToken("No more tokens"),
    ExpectingToken("Expecting token {token} was {received-token}"),
    BreakLoop("Breaking loop")
}

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

@Mutable
data class NodeCollector internal constructor(
    internal val collection: MutableList<Any>,
    internal val tokens: Iterator<LexerToken>
)

typealias ConsumeResult = Either<Iterator<LexerToken>, NodeCollector>
typealias WithNodeCollector = Either<*, NodeCollector>

fun Iterator<LexerToken>.consume(predicate: (LexerToken) -> Boolean): ConsumeResult {
    if (hasNext()) {
        val item = next()
        return if (predicate(item)) {
            Either.Right(NodeCollector(mutableListOf(item), this))
        } else {
            Either.Left(cons(item))
        }
    }
    return Either.Left(this)
}

fun Iterator<LexerToken>.consume(type: TokenType): ConsumeResult = consume {
    it.type == type
}

fun Iterator<LexerToken>.consume(type: TokenType, text: String): ConsumeResult = consume {
    it.type == type && it.text == text
}

fun Either<Iterator<LexerToken>, NodeCollector>.or(predicate: (LexerToken) -> Boolean) =
    when (this) {
        is Either.Left -> value.consume(predicate)
        is Either.Right -> this
    }


fun Either<Iterator<LexerToken>, NodeCollector>.or(type: TokenType) = or {
    it.type == type
}

fun Either<Iterator<LexerToken>, NodeCollector>.or(type: TokenType, text: String) = or {
    it.type == type && it.text == text
}

fun WithNodeCollector.then(
    predicate: (LexerToken) -> Boolean,
    error: (LexerToken) -> Error,
    discardToken: Boolean = false,
): ErrorOrValue<NodeCollector> =
    this.flatMap {
        val iterator = it.tokens
        if (iterator.hasNext()) {
            val item = iterator.next()
            if (predicate(item)) {
                if (!discardToken) it.collection.add(item)
                Either.Right(it)
            } else {
                Either.Left(error(item))
            }
        } else Either.Left(BaseParserErrorCode.EofToken.new())
    }

fun WithNodeCollector.then(
    type: TokenType,
    discardToken: Boolean = false
) = then({
    it.type == type
}, {
    BaseParserErrorCode.ExpectingToken.new("token" to "<$type>", "received-token" to "${it.type}:${it.text}")
}, discardToken)

fun WithNodeCollector.then(
    type: TokenType,
    text: String,
    discardToken: Boolean = false
) = then({
    it.type == type
}, {
    BaseParserErrorCode.ExpectingToken.new("token" to "'$type:$text'", "received-token" to "'${it.type}:${it.text}'")
}, discardToken)

fun <L, T> Either<L, NodeCollector>.build(block: (items: List<Any>) -> T): Either<L, ParserValue<T>> =
    map {
        ParserValue(block(it.collection.toList()), it.tokens)
    }

fun <T : Any> WithNodeCollector.thenLoop(block: (Iterator<LexerToken>) -> Either<*, ParserValue<T>>): ErrorOrValue<NodeCollector> =
    this.flatMap {
        val returnValue: NodeCollector
        while (true) {
            val result = it.tokens.lookAHead { lexer ->
                when (val result = block(lexer)) {
                    is Either.Right -> result.value.value.asLookAHeadResult()
                    is Either.Left -> BaseParserErrorCode.BreakLoop.new().asLookAHeadResult<T>()
                }
            }
            if (result is Either.Right) {
                it.collection.add(result.value.value as Any)
                continue
            }
            if (result is Either.Left<ParserError>) {
                returnValue = NodeCollector(
                    it.collection,
                    result.value.remainTokens
                )
                break
            }
        }
        Either.Right(returnValue)
    }