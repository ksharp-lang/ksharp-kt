package org.ksharp.parser

import org.ksharp.common.*
import org.ksharp.common.annotation.Mutable
import java.util.*
import kotlin.math.max

private const val BufferSize: Int = 10

internal class LookAheadBuffer {
    private var buffer = arrayOfNulls<Token>(BufferSize)
    private val checkPoints = Stack<Int>()

    private var tmpCheckpointIndex = -1
    private var endIndex = 0
    private var currentIndex = 0

    val checkpoints: Int get() = checkPoints.size
    fun addCheckpoint() {
        checkPoints.push(currentIndex)
        tmpCheckpointIndex = -1
    }

    fun removeCheckPoint(consumeTokens: Boolean) {
        val startIndex = checkPoints.pop()

        if (consumeTokens) {
            if (checkPoints.isEmpty()) {
                clear()
            }
            return
        }

        currentIndex = startIndex
        tmpCheckpointIndex = endIndex
    }

    fun next(fallback: () -> Token?): Token? =
        if (checkPoints.isNotEmpty() || tmpCheckpointIndex != -1) {
            if (currentIndex == endIndex) {
                val result = fallback()
                if (result != null) {
                    addToBuffer(result)
                    currentIndex = endIndex
                }
                result
            } else {
                val result = buffer[currentIndex++]
                if (currentIndex == tmpCheckpointIndex) {
                    checkPoints.clear()
                    clear()
                }
                result
            }
        } else fallback()

    fun rewind(move: Int) {
        currentIndex = (currentIndex - move).coerceAtLeast(0)
    }

    fun cons(token: Token) {
        if (checkPoints.isEmpty()) {
            addCheckpoint()
            addToBuffer(token)
            currentIndex = endIndex
            tmpCheckpointIndex = endIndex
        }
        currentIndex -= 1
    }

    fun cons(tokens: List<Token>) {
        val size = tokens.size
        if (checkPoints.isEmpty()) {
            addCheckpoint()
            ensureBufferFor(size)
            tokens.forEach { token ->
                buffer[currentIndex++] = token
            }
            endIndex = currentIndex
            tmpCheckpointIndex = endIndex
        }
        currentIndex -= size
    }

    private fun addToBuffer(token: Token) {
        ensureBufferFor(1)
        buffer[currentIndex] = token
        endIndex = currentIndex + 1
    }

    private fun ensureBufferFor(newItems: Int) {
        if (currentIndex + newItems >= buffer.size) {
            val newBuffer = arrayOfNulls<Token>(buffer.size + max(BufferSize, newItems))
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.size)
            buffer = newBuffer
        }
    }

    private fun clear() {
        currentIndex = 0
        endIndex = 0
        tmpCheckpointIndex = -1
    }

    private fun trim() {
        System.arraycopy(buffer, currentIndex, buffer, 0, endIndex)
        currentIndex = 0
    }
}

class LookAheadLexerState {
    private val checkpoints: Stack<Stack<Token>> = Stack()

    var enabled = false
        private set

    var lexer: BaseLexerIterator<*>? = null
        private set

    private var rootLexer: BaseLexerIterator<*>? = null
    private var pendingTokens = mutableListOf<Token>()

    fun addCheckPoint() {
        val stack = Stack<Token>()
        if (pendingTokens.isNotEmpty()) {
            if (checkpoints.isNotEmpty()) {
                checkpoints.peek().apply {
                    val pendingIndex = indexOf(pendingTokens.first())
                    if (pendingIndex != -1) repeat(size - pendingIndex) { pop() }
                }
            }
            stack.addAll(pendingTokens)
        }
        checkpoints.add(stack)
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
            lexer.next().also {
                collectValue(it)
            }
        } else null
    } else pendingTokens.removeFirst())

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
