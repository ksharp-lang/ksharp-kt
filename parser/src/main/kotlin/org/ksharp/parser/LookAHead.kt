package org.ksharp.parser

import org.ksharp.common.*
import org.ksharp.common.annotation.Mutable
import java.util.*

class LookAheadLexerState {
    private val checkpoints: Stack<Stack<Token>> = Stack()

    var enabled = false
        private set

    var lexer: BaseLexerIterator<*>? = null
        private set

    private var rootLexer: BaseLexerIterator<*>? = null
    private var pendingTokens = mutableListOf<Token>()

    fun addCheckPoint() {
        checkpoints.add(Stack())
    }

    fun removeCheckPoint(consumeTokens: Boolean) {
        val lastCheckPoint = checkpoints.pop()
        if (!consumeTokens) {
            pendingTokens = lastCheckPoint
        } else {
            if (!checkpoints.isEmpty()) {
                checkpoints.peek().addAll(lastCheckPoint)
            }
        }
    }

    private fun collectValue(token: Token) {
        if (!checkpoints.isEmpty()) {
            checkpoints.peek().push(token)
        }
    }

    private fun nextToken(): Token? = (if (pendingTokens.isEmpty()) {
        val lexer = rootLexer!!
        if (lexer.hasNext()) {
            lexer.next()
        } else null
    } else pendingTokens.removeFirst())?.also {
        collectValue(it)
    }

    fun <S> enable(
        lexer: BaseLexerIterator<S>
    ): BaseLexerIterator<S> {
        if (!enabled) {
            this.enabled = true
            this.rootLexer = lexer.cast()
            val lexerResult = generateLexerIterator(lexer.state) {
                nextToken()
            }
            this.lexer = lexerResult.cast()
        }
        return this.lexer!!.cast()
    }

    internal fun addPendingTokens(tokens: List<Token>) {
        pendingTokens.addAll(0, tokens)
    }

    internal fun addPendingToken(token: Token) {
        pendingTokens.add(0, token)
    }

}

data class LookAHeadResult<T>(
    val value: T
)

fun <T : Any, S> ParserResult<T, S>.asLookAHeadResult(): ErrorOrValue<LookAHeadResult<T>> =
    when (this) {
        is Either.Left -> value.error.asLookAHeadResult()
        is Either.Right -> value.value.asLookAHeadResult()
    }


fun <T : Any> Error.asLookAHeadResult(): ErrorOrValue<LookAHeadResult<T>> =
    Either.Left(this)

fun <T> T.asLookAHeadResult(): ErrorOrValue<LookAHeadResult<T>> =
    Either.Right(LookAHeadResult(this))

fun <S> BaseLexerIterator<S>.enableLookAhead(): BaseLexerIterator<S> =
    state.lookAHeadState.enable(this)

@Mutable
fun <T, S> BaseLexerIterator<S>.lookAHead(block: (BaseLexerIterator<S>) -> ErrorOrValue<LookAHeadResult<T>>): ParserResult<T, S> {
    val lookAheadLexerState = state.lookAHeadState
    if (!lookAheadLexerState.enabled) {
        ParserError(
            BaseParserErrorCode.LookAHeadNotEnabled.new(Location.NoProvided),
            listBuilder(),
            false,
            this
        )
    }

    lookAheadLexerState.addCheckPoint()

    val result = block(this)

    return result.map {
        lookAheadLexerState.removeCheckPoint(true)
        ParserValue(it.value, this)
    }.mapLeft {
        lookAheadLexerState.removeCheckPoint(false)
        ParserError(
            it,
            listBuilder(),
            false,
            this
        )
    }
}
