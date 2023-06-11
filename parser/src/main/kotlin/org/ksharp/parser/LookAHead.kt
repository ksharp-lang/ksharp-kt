package org.ksharp.parser

import org.ksharp.common.*
import org.ksharp.common.annotation.Mutable
import java.util.*

interface WithLookAHeadState {
    val enabled: Boolean
    fun <S> enable(lexer: BaseLexerIterator<S>)

    fun removeCheckPoint(consumeTokens: Boolean)

    fun addCheckPoint()

    fun <S : WithLookAHeadState> lexer(): BaseLexerIterator<S>

}

class LookAheadLexerState : WithLookAHeadState {

    private val checkpoints: Stack<Stack<Token>> = Stack()
    override var enabled: Boolean = false
        private set
    var lexer: BaseLexerIterator<*>? = null
        private set

    private var rootLexer: BaseLexerIterator<*>? = null
    private var pendingTokens: Stack<Token> = Stack()

    override fun addCheckPoint() {
        checkpoints.add(Stack())
    }

    override fun removeCheckPoint(consumeTokens: Boolean) {
        val lastCheckPoint = checkpoints.pop()
        if (!consumeTokens) {
            pendingTokens = lastCheckPoint
        } else if (!checkpoints.isEmpty()) {
            checkpoints.peek().addAll(lastCheckPoint)
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

    override fun <S> enable(
        lexer: BaseLexerIterator<S>
    ) {
        if (!enabled) {
            this.enabled = true
            this.rootLexer = lexer.cast()
            val lexerResult = generateLexerIterator(lexer.state) {
                nextToken()
            }
            this.lexer = lexerResult.cast()
        }
    }

    override fun <S : WithLookAHeadState> lexer(): BaseLexerIterator<S> = lexer!!.cast()

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


fun <S : WithLookAHeadState> BaseLexerIterator<S>.enableLookAHead(): BaseLexerIterator<S> = state.value.let {
    it.enable(this)
    it.lexer()
}

@Mutable
fun <T, S : WithLookAHeadState> BaseLexerIterator<S>.lookAHead(block: (BaseLexerIterator<S>) -> ErrorOrValue<LookAHeadResult<T>>): ParserResult<T, S> {
    val lookAheadLexerState = state.value
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
        ParserValue(it.value, lookAheadLexerState.lexer<S>())
    }.mapLeft {
        lookAheadLexerState.removeCheckPoint(false)
        ParserError(
            it,
            listBuilder(),
            false,
            lookAheadLexerState.lexer<S>()
        )
    }
}
