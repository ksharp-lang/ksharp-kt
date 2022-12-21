package org.ksharp.parser

import org.ksharp.common.*
import org.ksharp.common.annotation.Mutable

enum class BaseParserErrorCode(override val description: String) : ErrorCode {
    EofToken("No more tokens"),
    ExpectingToken("Expecting token {token} was {received-token}"),
    BreakLoop("Breaking loop"),
    ConsumeTokenFailed("Consume token failed")
}

data class ParserValue<T, LV>(
    val value: T,
    val remainTokens: Iterator<LV>
)

data class ParserError<LV>(
    val error: Error,
    val remainTokens: Iterator<LV>
)

typealias ParserResult<T, LV> = Either<ParserError<LV>, ParserValue<T, LV>>
typealias ParserErrorOrValue<LV, V> = Either<ParserError<LV>, V>

val <T, LV : LexerValue> ParserResult<T, LV>.remainTokens
    get() =
        when (this) {
            is Either.Left -> value.remainTokens
            is Either.Right -> value.remainTokens
        }

@Mutable
data class NodeCollector<T> internal constructor(
    internal val collection: ListBuilder<Any>,
    internal val tokens: Iterator<T>
)

typealias ConsumeResult<T> = Either<ParserError<T>, NodeCollector<T>>

fun <T : LexerValue> Iterator<T>.consume(predicate: (T) -> Boolean, discardToken: Boolean = false): ConsumeResult<T> {
    if (hasNext()) {
        val item = next()
        return if (predicate(item)) {
            Either.Right(
                NodeCollector(
                    listBuilder<Any>().apply {
                        if (!discardToken) add(item)
                    },
                    this
                )
            )
        } else {
            Either.Left(ParserError(BaseParserErrorCode.ConsumeTokenFailed.new(), cons(item)))
        }
    }
    return Either.Left(ParserError(BaseParserErrorCode.ConsumeTokenFailed.new(), this))
}

fun <T : LexerValue> Iterator<T>.consume(type: TokenType, discardToken: Boolean = false): ConsumeResult<T> = consume({
    it.type == type
}, discardToken)

fun <T : LexerValue> Iterator<T>.consume(
    type: TokenType,
    text: String,
    discardToken: Boolean = false
): ConsumeResult<T> = consume({
    it.type == type && it.text == text
}, discardToken)

fun <LV : LexerValue, T> ParserResult<T, LV>.or(
    rule: Iterator<LV>.() -> ParserResult<T, LV>
) =
    when (this) {
        is Either.Left -> rule(value.remainTokens)
        is Either.Right -> this
    }

fun <T : LexerValue> ConsumeResult<T>.then(
    predicate: (T) -> Boolean,
    error: (T) -> Error,
    discardToken: Boolean = false,
): ConsumeResult<T> =
    this.flatMap {
        val iterator = it.tokens
        if (iterator.hasNext()) {
            val item = iterator.next()
            if (predicate(item)) {
                if (!discardToken) it.collection.add(item)
                Either.Right(it)
            } else {
                Either.Left(ParserError(error(item), iterator))
            }
        } else Either.Left(ParserError(BaseParserErrorCode.EofToken.new(), iterator))
    }

fun <T : LexerValue> ConsumeResult<T>.then(
    type: TokenType,
    discardToken: Boolean = false
) = then({
    it.type == type
}, {
    BaseParserErrorCode.ExpectingToken.new("token" to "<$type>", "received-token" to "${it.type}:${it.text}")
}, discardToken)

fun <T : LexerValue> ConsumeResult<T>.then(
    type: TokenType,
    text: String,
    discardToken: Boolean = false
) = then({
    it.type == type
}, {
    BaseParserErrorCode.ExpectingToken.new("token" to "'$type:$text'", "received-token" to "'${it.type}:${it.text}'")
}, discardToken)

fun <T, LV : LexerValue> ConsumeResult<LV>.build(block: (items: List<Any>) -> T): ParserResult<T, LV> =
    map {
        ParserValue(block(it.collection.build()), it.tokens)
    }

fun <L, T, LV : LexerValue> Either<L, ParserValue<T, LV>>.resume() =
    map {
        val items = listBuilder<Any>()
        items.add(it.value as Any)
        NodeCollector(
            items,
            it.remainTokens
        )
    }

fun <T : Any, LV : LexerValue> ConsumeResult<LV>.thenLoop(block: (Iterator<LV>) -> ParserResult<T, LV>): ConsumeResult<LV> =
    this.flatMap {
        val returnValue: NodeCollector<LV>
        var tokens = it.tokens
        while (true) {
            val result = tokens.lookAHead { lexer ->
                when (val result = block(lexer)) {
                    is Either.Right -> result.value.value.asLookAHeadResult(result.remainTokens)
                    is Either.Left -> BaseParserErrorCode.BreakLoop.new().asLookAHeadResult()
                }
            }
            if (result is Either.Right) {
                it.collection.add(result.value.value as Any)
                tokens = result.remainTokens
                continue
            }
            if (result is Either.Left) {
                returnValue = NodeCollector(
                    it.collection,
                    result.value.remainTokens
                )
                break
            }
        }
        Either.Right(returnValue)
    }

fun <T : LexerValue> Iterator<T>.collect(): ConsumeResult<T> =
    Either.Right(NodeCollector(listBuilder(), this))


fun <T, LV : LexerValue> ConsumeResult<LV>.consume(block: (Iterator<LV>) -> ParserResult<T, LV>): ConsumeResult<LV> =
    flatMap { collector ->
        block(collector.tokens).map {
            collector.collection.add(it.value as Any)
            NodeCollector(
                collector.collection,
                it.remainTokens
            )
        }
    }