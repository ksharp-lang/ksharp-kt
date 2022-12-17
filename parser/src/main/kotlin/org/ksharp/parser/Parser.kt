package org.ksharp.parser

import org.ksharp.common.*
import org.ksharp.common.annotation.Mutable

enum class BaseParserErrorCode(override val description: String) : ErrorCode {
    EofToken("No more tokens"),
    ExpectingToken("Expecting token {token} was {received-token}"),
    BreakLoop("Breaking loop")
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

typealias ConsumeResult<T> = Either<Iterator<T>, NodeCollector<T>>
typealias WithNodeCollector<T> = Either<*, NodeCollector<T>>

fun <T : LexerValue> Iterator<T>.consume(predicate: (T) -> Boolean): ConsumeResult<T> {
    if (hasNext()) {
        val item = next()
        return if (predicate(item)) {
            Either.Right(
                NodeCollector(
                    listBuilder<Any>().apply {
                        add(item)
                    },
                    this
                )
            )
        } else {
            Either.Left(cons(item))
        }
    }
    return Either.Left(this)
}

fun <T : LexerValue> Iterator<T>.consume(type: TokenType): ConsumeResult<T> = consume {
    it.type == type
}

fun <T : LexerValue> Iterator<T>.consume(type: TokenType, text: String): ConsumeResult<T> = consume {
    it.type == type && it.text == text
}

fun <T : LexerValue> Either<Iterator<T>, NodeCollector<T>>.or(predicate: (T) -> Boolean) =
    when (this) {
        is Either.Left -> value.consume(predicate)
        is Either.Right -> this
    }


fun <T : LexerValue> Either<Iterator<T>, NodeCollector<T>>.or(type: TokenType) = or {
    it.type == type
}

fun <T : LexerValue> Either<Iterator<T>, NodeCollector<T>>.or(type: TokenType, text: String) = or {
    it.type == type && it.text == text
}

fun <T : LexerValue> WithNodeCollector<T>.then(
    predicate: (T) -> Boolean,
    error: (T) -> Error,
    discardToken: Boolean = false,
): ErrorOrValue<NodeCollector<T>> =
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

fun <T : LexerValue> WithNodeCollector<T>.then(
    type: TokenType,
    discardToken: Boolean = false
) = then({
    it.type == type
}, {
    BaseParserErrorCode.ExpectingToken.new("token" to "<$type>", "received-token" to "${it.type}:${it.text}")
}, discardToken)

fun <T : LexerValue> WithNodeCollector<T>.then(
    type: TokenType,
    text: String,
    discardToken: Boolean = false
) = then({
    it.type == type
}, {
    BaseParserErrorCode.ExpectingToken.new("token" to "'$type:$text'", "received-token" to "'${it.type}:${it.text}'")
}, discardToken)

fun <L, T, LV : LexerValue> Either<L, NodeCollector<LV>>.build(block: (items: List<Any>) -> T): Either<L, ParserValue<T, LV>> =
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

fun <T : Any, LV : LexerValue> WithNodeCollector<LV>.thenLoopIndexed(block: (Iterator<LV>, index: Int) -> Either<*, ParserValue<T, LV>>): ErrorOrValue<NodeCollector<LV>> =
    this.flatMap {
        val returnValue: NodeCollector<LV>
        var index = 0
        while (true) {
            val result = it.tokens.lookAHead { lexer ->
                when (val result = block(lexer, index)) {
                    is Either.Right -> result.value.value.asLookAHeadResult()
                    is Either.Left -> BaseParserErrorCode.BreakLoop.new().asLookAHeadResult()
                }
            }
            index += 1
            if (result is Either.Right) {
                it.collection.add(result.value.value as Any)
                continue
            }
            if (result is Either.Left<ParserError<LV>>) {
                returnValue = NodeCollector(
                    it.collection,
                    result.value.remainTokens
                )
                break
            }
        }
        Either.Right(returnValue)
    }

fun <T : Any, LV : LexerValue> WithNodeCollector<LV>.thenLoop(block: (Iterator<LV>) -> Either<*, ParserValue<T, LV>>): ErrorOrValue<NodeCollector<LV>> =
    thenLoopIndexed { iterator, _ ->
        block(iterator)
    }

fun <T : LexerValue> Iterator<T>.collect(): WithNodeCollector<T> =
    Either.Right(NodeCollector(listBuilder(), this))


fun <L, T, LV : LexerValue> WithNodeCollector<LV>.consume(block: (Iterator<LV>) -> Either<L, ParserValue<T, LV>>): WithNodeCollector<LV> =
    flatMap { collector ->
        block(collector.tokens).map {
            collector.collection.add(it.value as Any)
            NodeCollector(
                collector.collection,
                it.remainTokens
            )
        }
    }