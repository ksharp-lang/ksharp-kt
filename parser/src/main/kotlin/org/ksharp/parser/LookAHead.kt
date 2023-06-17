package org.ksharp.parser

import org.ksharp.common.*
import org.ksharp.common.annotation.Mutable
import java.util.*

interface LookAheadBuffer {
    fun release()
    fun recover()

    /**
     * Release the buffer and add at the beginning of the stack a token
     */
    fun releaseWith(token: Token)
}

class LookAheadLexerState {
    private val checkpoints: Stack<Stack<Token>> = Stack()

    var enabled = false
        private set

    private var bufferEnabled = false

    var lexer: BaseLexerIterator<*>? = null
        private set

    private var rootLexer: BaseLexerIterator<*>? = null
    private var pendingTokens = mutableListOf<Token>()
    private val buffer = mutableListOf<Token>()

    fun addCheckPoint() {
        if (bufferEnabled) {
            throw RuntimeException("Can't add an endpoint when there is a buffer active")
        }
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
        addBuffer(token)
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

    private fun addBuffer(token: Token) {
        if (bufferEnabled) {
            buffer.add(token)
        }
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

    fun buffer(): LookAheadBuffer {
        if (bufferEnabled) {
            throw RuntimeException("A buffer already was created")
        }
        bufferEnabled = true
        buffer.clear()
        return object : LookAheadBuffer {

            override fun release() {
                bufferEnabled = false
            }

            override fun recover() {
                bufferEnabled = false
                pendingTokens.addAll(buffer)
            }

            override fun releaseWith(token: Token) {
                bufferEnabled = false
                pendingTokens.add(token)
            }
        }
    }

    internal fun addPendingTokens(tokens: List<Token>) {
        pendingTokens.addAll(0, tokens)
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
