package org.ksharp.parser.ksharp

import org.ksharp.common.Either
import org.ksharp.common.Flag
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


class IndentationOffset {

    private val offsets = Stack<OffsetImpl>()
    private val newLine = Flag() //TODO review delete

    val currentType: OffsetType get() = if (offsets.isEmpty()) OffsetType.Normal else offsets.peek().type
    val currentOffset: Offset get() = offsets.peek()

    private fun update(size: Int, sameResult: OffsetAction): OffsetAction {
        if (offsets.isEmpty()) {
            newLine.activate()
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

        newLine.activate()
        return if (last.type == OffsetType.Repeating) {
            OffsetAction.Repeating
        } else sameResult
    }

    fun add(size: Int, type: OffsetType): Boolean {
        val allowed = if (offsets.isEmpty()) true else
            if (type != OffsetType.Optional) offsets.peek().size <= size
            else offsets.peek().size < size

        if (allowed) offsets.push(OffsetImpl(size, false, type))
        return allowed
    }

    fun addRelative(size: Int, type: OffsetType) {
        val position = if (offsets.isEmpty()) 0 else {
            offsets.peek().size + size
        }
        offsets.push(OffsetImpl(position, false, type))
    }

    fun update(size: Int) = update(size, OffsetAction.Same)

}


fun KSharpLexerIterator.addIndentationOffset(
    type: OffsetType,
    useStartTokenOffset: Boolean = false
): KSharpLexerIterator {
    val lexerState = state.value
    lexerState.indentationOffset.add(
        if (useStartTokenOffset) lastStartOffset else lastEndOffset - lexerState.lineStartOffset.get(),
        type
    )
    return this
}

fun KSharpLexerIterator.addRelativeIndentationOffset(relative: Int, type: OffsetType): KSharpLexerIterator {
    val lexerState = state.value
    lexerState.indentationOffset.addRelative(relative, type)
    return this
}

@JvmName("addIndentationOffset2")
fun <S> ParserResult<S, KSharpLexerState>.addIndentationOffset(
    type: OffsetType,
    useStartTokenOffset: Boolean = false
): ParserResult<S, KSharpLexerState> =
    map {
        it.remainTokens.addIndentationOffset(type, useStartTokenOffset)
        it
    }

fun KSharpConsumeResult.addIndentationOffset(
    type: OffsetType,
    useStartTokenOffset: Boolean = false
): KSharpConsumeResult =
    map {
        it.tokens.addIndentationOffset(type, useStartTokenOffset)
        it
    }

fun KSharpConsumeResult.addRelativeIndentationOffset(relative: Int, type: OffsetType): KSharpConsumeResult =
    map {
        assert(relative > 0) { "relative offsets should be greater than zero" }
        it.tokens.addRelativeIndentationOffset(relative, type)
        it
    }

fun KSharpLexerIterator.enableIndentationOffset(): KSharpLexerIterator {
    val lexerState = state.value
    val indentationOffset = lexerState.indentationOffset
    return generateLexerIterator(state) {
        while (hasNext()) {
            val token = next()
            if (token.type == BaseTokenType.NewLine) {
                lexerState.lineStartOffset.set(lastStartOffset)
                val indentLength = token.text.indentLength() - 1
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

fun KSharpConsumeResult.thenReapingIndentation(
    requireAlwaysNewLine: Boolean,
    block: (KSharpConsumeResult) -> KSharpParserResult
): KSharpConsumeResult =
    flatMap {
        val lexer = it.tokens.addRelativeIndentationOffset(1, OffsetType.Repeating)
        val indentationOffset = lexer.state.value.indentationOffset
        val offset = indentationOffset.currentOffset
        val tokenPredicate: (Token) -> Boolean = { tk ->
            tk.type == BaseTokenType.NewLine && indentationOffset.currentOffset == offset
        }
        Either.Right(it)
            .thenLoopIndexed { l, index ->
                if (requireAlwaysNewLine) {
                    l.consume(tokenPredicate, true)
                        .let(block)
                } else
                    l.ifConsume(tokenPredicate, true, block).or {
                        block(l.collect())
                    }
            }
    }

fun KSharpConsumeResult.withIndentationOffset(
    useStartTokenOffset: Boolean,
    block: (KSharpConsumeResult) -> KSharpConsumeResult
): KSharpConsumeResult =
    flatMap {
        val lexer = it.tokens.addIndentationOffset(OffsetType.Normal, useStartTokenOffset)
        val indentationOffset = lexer.state.value.indentationOffset
        val offset = indentationOffset.currentOffset
        val tokenPredicate: (Token) -> Boolean = { tk ->
            tk.type == BaseTokenType.NewLine && indentationOffset.currentOffset == offset
        }
        block(Either.Right(it))
            .thenOptional(tokenPredicate, true)
    }
