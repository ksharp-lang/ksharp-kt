package org.ksharp.parser.ksharp

import org.ksharp.common.Either
import org.ksharp.parser.*
import java.util.*

interface Offset {
    val size: Int
    val fixed: Boolean
    val type: OffsetType
}

private data class OffsetImpl(override var size: Int, override var fixed: Boolean, override var type: OffsetType) :
    Offset

enum class OffsetAction {
    Invalid,
    Same,
    Repeating,
    Previous,
    End
}

enum class OffsetType(val action: OffsetAction) {
    Normal(OffsetAction.Previous),
    Optional(OffsetAction.Same),
    Repeating(OffsetAction.Repeating)
}


private val EmptyOffset = OffsetImpl(0, true, OffsetType.Optional)

class IndentationOffset {

    private val offsets = Stack<OffsetImpl>()
    val currentOffset: Offset get() = if (offsets.isEmpty()) EmptyOffset else offsets.peek()

    private fun update(size: Int, sameResult: OffsetAction): OffsetAction {
        if (offsets.isEmpty()) {
            return OffsetAction.End
        }

        val last = offsets.peek()
        if (last.size > size) {
            return update(size, offsets.pop().type.action)
        }

        if (last.size < size) {
            if (last.fixed) return OffsetAction.Invalid
            last.size = size
            last.fixed = true
            if (last.type == OffsetType.Optional)
                last.type = OffsetType.Normal
        }
        return if (last.type == OffsetType.Repeating) {
            OffsetAction.Repeating
        } else sameResult
    }

    fun add(size: Int, type: OffsetType): Boolean {
        val allowed = offsets.isEmpty() || offsets.peek().size <= size
        if (allowed) {
            if (offsets.isNotEmpty()) {
                val last = offsets.peek()
                if (last.type == OffsetType.Optional && last.size == size) {
                    last.type = type
                    return true
                }
            }
            offsets.push(OffsetImpl(size, false, type))
        }
        return allowed
    }

    fun addRelative(type: OffsetType) {
        val position = if (offsets.isEmpty()) 0 else {
            offsets.peek().size + 1
        }
        offsets.push(OffsetImpl(position, false, type))
    }

    fun update(size: Int) = update(size, OffsetAction.Same)

    fun remove(offset: Offset) {
        if (offsets.isNotEmpty() && offsets.peek() == offset) {
            offsets.pop()
        }
    }

}

enum class IndentationOffsetType {
    StartOffset,
    EndOffset,
    Relative,
    NextToken
}

private fun KSharpLexerIterator.addIndentationOffset(
    indentationType: IndentationOffsetType = IndentationOffsetType.EndOffset,
    type: OffsetType = OffsetType.Optional,
): KSharpLexerIterator {
    val lexerState = state.value
    val indentationOffset = lexerState.indentationOffset

    when (indentationType) {
        IndentationOffsetType.StartOffset -> indentationOffset.add(
            lastStartOffset - lexerState.lineStartOffset.get(),
            type
        )

        IndentationOffsetType.EndOffset -> indentationOffset.add(lastEndOffset - lexerState.lineStartOffset.get(), type)
        IndentationOffsetType.Relative -> indentationOffset.addRelative(type)
        IndentationOffsetType.NextToken -> {
            val lookAHeadCheckPoint = state.lookAHeadState.checkpoint()
            if (hasNext()) {
                next()
                indentationOffset.add(
                    lastStartOffset - lexerState.lineStartOffset.get(),
                    type
                )
            }
            lookAHeadCheckPoint.end(PreserveTokens)
        }
    }
    return this
}

fun KSharpConsumeResult.addRelativeIndentationOffset(type: OffsetType): KSharpConsumeResult =
    map {
        it.tokens.addIndentationOffset(IndentationOffsetType.Relative, type)
        it
    }

private val Token.newLineStartOffset: Int
    get() =
        startOffset + (if (text.startsWith("\r\n")) 2 else 1)

fun KSharpLexerIterator.enableIndentationOffset(): KSharpLexerIterator {
    val lexerState = state.value
    val indentationOffset = lexerState.indentationOffset
    return generateLexerIterator(state) {
        while (hasNext()) {
            val token = next()
            if (token.type == BaseTokenType.NewLine) {
                val indentLength = token.text.indentLength() - 1
                lexerState.lineStartOffset.set(token.newLineStartOffset)
                when (indentationOffset.update(indentLength)) {
                    OffsetAction.Same -> if (indentLength == 0) Unit else continue
                    else -> Unit
                }
            }
            return@generateLexerIterator token
        }
        null
    }
}

fun KSharpConsumeResult.thenRepeatingIndentation(
    requireAlwaysNewLine: Boolean,
    block: (KSharpConsumeResult) -> KSharpParserResult
): KSharpConsumeResult =
    flatMap {
        val lexer = it.tokens.addIndentationOffset(IndentationOffsetType.Relative, OffsetType.Repeating)
        val indentationOffset = lexer.state.value.indentationOffset
        val offset = indentationOffset.currentOffset
        val tokenPredicate: (Token) -> Boolean = { tk ->
            tk.type == BaseTokenType.NewLine && indentationOffset.currentOffset == offset
        }
        Either.Right(it)
            .thenLoopIndexed { l, index ->
                if (index != 0 || requireAlwaysNewLine) {
                    l.consume(tokenPredicate, true)
                        .let(block)
                } else
                    l.ifConsume(tokenPredicate, true, block).or {
                        block(l.collect())
                    }
            }.also { indentationOffset.remove(offset) }
    }

fun KSharpConsumeResult.withAlignedIndentationOffset(
    indentationType: IndentationOffsetType,
    block: (KSharpConsumeResult) -> KSharpConsumeResult
): KSharpConsumeResult =
    flatMap {
        val lexer = it.tokens.addIndentationOffset(indentationType, OffsetType.Normal)
        val indentationOffset = lexer.state.value.indentationOffset
        val offset = indentationOffset.currentOffset
        val tokenPredicate: (Token) -> Boolean = { tk ->
            tk.type == BaseTokenType.NewLine && indentationOffset.currentOffset == offset
        }
        block(Either.Right(it))
            .thenOptional(tokenPredicate, true)
            .also {
                indentationOffset.remove(offset)
            }
    }

fun <T> KSharpLexerIterator.withNextTokenIndentationOffset(
    type: OffsetType,
    block: (KSharpLexerIterator) -> T
): T {
    val lexerState = state.value
    val indentationOffset = lexerState.indentationOffset
    addIndentationOffset(IndentationOffsetType.NextToken, type)
    val currentOffset = indentationOffset.currentOffset
    return block(this).also {
        indentationOffset.remove(currentOffset)
    }
}

fun <T> KSharpLexerIterator.withIndentationOffset(
    indentationType: IndentationOffsetType,
    type: OffsetType,
    block: (KSharpLexerIterator) -> T
): T {
    val indentationOffset = state.value.indentationOffset
    addIndentationOffset(indentationType, type)
    val currentOffset = indentationOffset.currentOffset
    return block(this).also {
        indentationOffset.remove(currentOffset)
    }
}

fun KSharpConsumeResult.withIndentationOffset(
    indentationType: IndentationOffsetType,
    type: OffsetType,
    block: (KSharpLexerIterator) -> KSharpParserResult
): KSharpConsumeResult = consume {
    it.withIndentationOffset(indentationType, type, block)
}
