package org.ksharp.parser

import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.common.ErrorOrValue
import org.ksharp.common.annotation.Mutable
import org.ksharp.common.listBuilder
import java.util.*

private const val BufferSize: Int = 10

const val ConsumeTokens: Int = 0
const val PreserveTokens: Int = -1


internal class LookAheadCheckpoints {
    private var buffer = arrayOfNulls<Token>(BufferSize)
    private val checkPoints = Stack<Int>()

    private var inCheckPoint = false
    private var endIndex = 0
    private var currentIndex = 0
    val checkpoints: Int get() = checkPoints.size
    fun addCheckpoint() {
        checkPoints.push(currentIndex)
        inCheckPoint = true
    }

    fun removeCheckPoint(rewind: Int) {
        val startIndex = checkPoints.pop()

        when (rewind) {
            ConsumeTokens -> {
                trim()
                if (checkPoints.isEmpty()) {
                    inCheckPoint = false
                }
            }

            PreserveTokens -> {
                currentIndex = startIndex
            }

            else -> {
                currentIndex -= rewind
                trim()
            }
        }
    }

    fun next(fallback: () -> Token?): Token? =
        if (inCheckPoint) {
            if (currentIndex == endIndex) {
                val result = fallback()
                if (result != null) {
                    addToBuffer(result)
                }
                result
            } else {
                val result = buffer[currentIndex++]
                if (currentIndex == endIndex && checkPoints.isEmpty()) {
                    clear()
                }
                result
            }
        } else fallback()


    private fun addToBuffer(token: Token) {
        val newSize = currentIndex + 1
        if (newSize >= buffer.size) {
            val newBuffer = arrayOfNulls<Token>(buffer.size + BufferSize)
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.size)
            buffer = newBuffer
        }
        buffer[currentIndex] = token
        currentIndex = newSize
        endIndex = newSize
    }

    private fun clear() {
        currentIndex = 0
        endIndex = 0
        inCheckPoint = false
    }

    private fun trim() {
        if (checkPoints.isEmpty()) {
            if (currentIndex != 0) {
                System.arraycopy(buffer, currentIndex, buffer, 0, endIndex)
                endIndex -= currentIndex
                currentIndex = 0
            }
        }
    }
}

interface LookAheadCheckpoint {
    fun end(rewind: Int)

}

class LookAheadLexerState {
    private val checkpoints = LookAheadCheckpoints()

    private var enabled = false

    fun <S> enable(
        lexer: BaseLexerIterator<S>
    ): BaseLexerIterator<S> =
        if (!enabled) {
            this.enabled = true
            generateLexerIterator(lexer.state) {
                checkpoints.next {
                    if (lexer.hasNext()) {
                        lexer.next()
                    } else null
                }
            }
        } else throw RuntimeException("LookAhead already enabled")

    fun checkpoint(): LookAheadCheckpoint {
        if (!enabled) throw RuntimeException("LookAhead not enabled")
        checkpoints.addCheckpoint()
        return object : LookAheadCheckpoint {
            override fun end(rewind: Int) {
                checkpoints.removeCheckPoint(rewind)
            }
        }
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
    val checkpoint = state.lookAHeadState.checkpoint()
    val result = block(this)
    return result.map {
        checkpoint.end(ConsumeTokens)
        ParserValue(it.value, this)
    }.mapLeft {
        checkpoint.end(PreserveTokens)
        ParserError(
            it,
            listBuilder(),
            false,
            this
        )
    }
}
