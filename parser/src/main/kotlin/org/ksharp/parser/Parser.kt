package org.ksharp.parser

import org.ksharp.common.*
import org.ksharp.common.annotation.Mutable

enum class BaseParserErrorCode(override val description: String) : ErrorCode {
    EofToken("No more tokens"),
    ExpectingToken("Expecting token {token} was {received-token}"),
    BreakLoop("Breaking loop"),
    ConsumeTokenFailed("Consume token failed {token}")
}

data class ParserValue<T, S>(
    val value: T,
    val remainTokens: BaseLexerIterator<S>
)

data class ParserError<S>(
    val error: Error,
    val consumedTokens: Boolean,
    val remainTokens: BaseLexerIterator<S>
)

typealias ParserResult<T, S> = Either<ParserError<S>, ParserValue<T, S>>
typealias ParserErrorOrValue<S, V> = Either<ParserError<S>, V>

val <T, S> ParserResult<T, S>.remainTokens
    get() =
        when (this) {
            is Either.Left -> value.remainTokens
            is Either.Right -> value.remainTokens
        }

@Mutable
data class NodeCollector<S> internal constructor(
    internal val collection: ListBuilder<Any>,
    internal val tokens: BaseLexerIterator<S>
) {
    val consumed: Boolean get() = collection.size() > 0
}

typealias ConsumeResult<S> = Either<ParserError<S>, NodeCollector<S>>

fun <S> BaseLexerIterator<S>.consume(predicate: (Token) -> Boolean, discardToken: Boolean = false): ConsumeResult<S> {
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
            Either.Left(
                ParserError(
                    BaseParserErrorCode.ConsumeTokenFailed.new("token" to "'${item.type}:${item.text}'"),
                    false,
                    cons(item)
                )
            )
        }
    }
    return Either.Left(ParserError(BaseParserErrorCode.ConsumeTokenFailed.new("token" to "<EOF>"), false, this))
}

fun <S> BaseLexerIterator<S>.optionalConsume(
    predicate: (Token) -> Boolean,
    discardToken: Boolean = false
): ConsumeResult<S> {
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
            Either.Right(
                NodeCollector(
                    listBuilder<Any>(),
                    this.cons(item)
                )
            )
        }
    }
    return Either.Left(ParserError(BaseParserErrorCode.ConsumeTokenFailed.new("token" to "<EOF>"), false, this))
}

fun <S> ConsumeResult<S>.thenOptional(
    predicate: (Token) -> Boolean,
    discardToken: Boolean
): ConsumeResult<S> =
    flatMap {
        if (it.tokens.hasNext()) {
            val item = it.tokens.next()
            if (predicate(item)) {
                if (!discardToken) it.collection.add(item)
                Either.Right(it)
            } else {
                Either.Right(
                    NodeCollector(
                        it.collection,
                        it.tokens.cons(item)
                    )
                )
            }
        } else Either.Left(
            ParserError(
                BaseParserErrorCode.ConsumeTokenFailed.new("token" to "<EOF>"),
                false,
                it.tokens
            )
        )
    }


fun <T, S> BaseLexerIterator<S>.ifConsume(
    predicate: (Token) -> Boolean,
    discardToken: Boolean = false,
    block: (tokens: ConsumeResult<S>) -> ParserResult<T, S>
): ParserResult<T, S> {
    if (hasNext()) {
        val item = next()
        return if (predicate(item)) {
            block(
                Either.Right(
                    NodeCollector(
                        listBuilder<Any>().apply {
                            if (!discardToken) add(item)
                        },
                        this
                    )
                )
            )
        } else {
            Either.Left(
                ParserError(
                    BaseParserErrorCode.ConsumeTokenFailed.new("token" to "'${item.type}:${item.text}'"),
                    false,
                    cons(item)
                )
            )
        }
    }
    return Either.Left(ParserError(BaseParserErrorCode.ConsumeTokenFailed.new("token" to "<EOF>"), false, this))
}


fun <S> BaseLexerIterator<S>.consume(type: TokenType, discardToken: Boolean = false): ConsumeResult<S> = consume({
    it.type == type
}, discardToken)

fun <S> BaseLexerIterator<S>.consume(
    type: TokenType,
    text: String,
    discardToken: Boolean = false
): ConsumeResult<S> = consume({
    it.type == type && it.text == text
}, discardToken)


fun <S> BaseLexerIterator<S>.optionalConsume(type: TokenType, discardToken: Boolean = false): ConsumeResult<S> =
    optionalConsume({
        it.type == type
    }, discardToken)

fun <S> ConsumeResult<S>.thenOptional(type: TokenType, discardToken: Boolean): ConsumeResult<S> =
    thenOptional({
        it.type == type
    }, discardToken)

fun <T, S> BaseLexerIterator<S>.ifConsume(
    type: TokenType,
    discardToken: Boolean = false,
    block: (tokens: ConsumeResult<S>) -> ParserResult<T, S>
): ParserResult<T, S> = ifConsume({
    it.type == type
}, discardToken, block)

fun <T, S> BaseLexerIterator<S>.ifConsume(
    type: TokenType,
    text: String,
    discardToken: Boolean = false,
    block: (tokens: ConsumeResult<S>) -> ParserResult<T, S>
): ParserResult<T, S> = ifConsume({
    it.type == type && it.text == text
}, discardToken, block)

fun <S, T> ParserResult<T, S>.or(
    rule: (tokens: BaseLexerIterator<S>) -> ParserResult<T, S>
) =
    when (this) {
        is Either.Left -> if (value.consumedTokens) {
            this
        } else rule(value.remainTokens)

        is Either.Right -> this
    }

fun <S> ConsumeResult<S>.then(
    predicate: (Token) -> Boolean,
    error: (Token) -> Error,
    discardToken: Boolean = false,
): ConsumeResult<S> =
    this.flatMap {
        val iterator = it.tokens
        if (iterator.hasNext()) {
            val item = iterator.next()
            if (predicate(item)) {
                if (!discardToken) it.collection.add(item)
                Either.Right(it)
            } else {
                Either.Left(ParserError(error(item), it.consumed, iterator.cons(item)))
            }
        } else Either.Left(ParserError(BaseParserErrorCode.EofToken.new(), it.consumed, iterator))
    }

fun <S> ConsumeResult<S>.then(
    type: TokenType,
    discardToken: Boolean = false
) = then({
    it.type == type
}, {
    BaseParserErrorCode.ExpectingToken.new("token" to "<$type>", "received-token" to "${it.type}:${it.text}")
}, discardToken)

fun <S> ConsumeResult<S>.then(
    type: TokenType,
    text: String,
    discardToken: Boolean = false
) = then({
    it.type == type && it.text == text
}, {
    BaseParserErrorCode.ExpectingToken.new("token" to "'$type:$text'", "received-token" to "'${it.type}:${it.text}'")
}, discardToken)

fun <T, S> ConsumeResult<S>.build(block: (items: List<Any>) -> T): ParserResult<T, S> =
    map {
        ParserValue(block(it.collection.build()), it.tokens)
    }

fun <L, T, S> Either<L, ParserValue<T, S>>.resume() =
    map {
        val items = listBuilder<Any>()
        items.add(it.value as Any)
        NodeCollector(
            items,
            it.remainTokens
        )
    }

fun <T : Any, S> ConsumeResult<S>.thenLoop(block: (BaseLexerIterator<S>) -> ParserResult<T, S>): ConsumeResult<S> =
    thenLoopIndexed { lexer, _ ->
        block(lexer)
    }

fun <T : Any, S> ConsumeResult<S>.thenLoopIndexed(block: (BaseLexerIterator<S>, index: Int) -> ParserResult<T, S>): ConsumeResult<S> =
    this.flatMap {
        val returnValue: NodeCollector<S>
        var tokens = it.tokens
        var index = -1
        while (true) {
            index += 1
            val result = tokens.lookAHead { lexer ->
                when (val result = block(lexer, index)) {
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

fun <S> ConsumeResult<S>.thenIf(
    predicate: (token: Token) -> Boolean,
    discardToken: Boolean = false,
    block: (ConsumeResult<S>) -> ConsumeResult<S>
): ConsumeResult<S> =
    this.flatMap {
        val tokens = it.tokens
        if (tokens.hasNext()) {
            val token = tokens.next()
            if (predicate(token)) {
                if (!discardToken) it.collection.add(token)
                block(
                    Either.Right(
                        NodeCollector(
                            it.collection,
                            tokens
                        )
                    )
                )
            } else Either.Right(
                NodeCollector(
                    it.collection,
                    tokens.cons(token)
                )
            )
        } else Either.Right(it)
    }

fun <S> ConsumeResult<S>.thenIf(
    type: TokenType,
    discardToken: Boolean = false,
    block: (ConsumeResult<S>) -> ConsumeResult<S>
): ConsumeResult<S> =
    thenIf({ it.type == type }, discardToken, block)

fun <S> ConsumeResult<S>.thenIf(
    type: TokenType,
    text: String,
    discardToken: Boolean = false,
    block: (ConsumeResult<S>) -> ConsumeResult<S>
): ConsumeResult<S> =
    thenIf({ it.type == type && it.text == text }, discardToken, block)


fun <S> BaseLexerIterator<S>.collect(): ConsumeResult<S> =
    Either.Right(NodeCollector(listBuilder(), this))


fun <T, S> ConsumeResult<S>.consume(block: (BaseLexerIterator<S>) -> ParserResult<T, S>): ConsumeResult<S> =
    flatMap { collector ->
        block(collector.tokens).map {
            collector.collection.add(it.value as Any)
            NodeCollector(
                collector.collection,
                it.remainTokens
            )
        }
    }