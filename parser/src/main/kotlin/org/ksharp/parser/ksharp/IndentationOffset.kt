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
        val (allowed, calculatedSize) = if (offsets.isEmpty()) true to size
        else if (offsets.peek().size <= size) {
            val last = offsets.peek()
            // avoid ambiguity
            if (last.type == OffsetType.Repeating && last.size == size) {
                true to (size + 1)
            } else true to size
        } else (false to 0)

        if (allowed) {
            offsets.push(OffsetImpl(calculatedSize, false, type))
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
        if (offsets.isNotEmpty() && offsets.peek() === offset) {
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
    indentationType: IndentationOffsetType,
    type: OffsetType,
): KSharpLexerIterator {
    val lexerState = state.value
    val indentationOffset = lexerState.indentationOffset

    when (indentationType) {
        IndentationOffsetType.StartOffset -> indentationOffset.add(
            lexerState.lineOffset.size(lastStartOffset),
            type
        )

        IndentationOffsetType.EndOffset -> indentationOffset.add(lexerState.lineOffset.size(lastEndOffset), type)
        IndentationOffsetType.Relative -> indentationOffset.addRelative(type)
        IndentationOffsetType.NextToken -> {
            val lookAHeadCheckPoint = state.lookAHeadState.checkpoint()
            if (hasNext()) {
                val t = next()
                if (t.type == BaseTokenType.NewLine) {
                    indentationOffset.addRelative(type)
                } else
                    indentationOffset.add(
                        lexerState.lineOffset.size(t.startOffset),
                        type
                    )
            }
            lookAHeadCheckPoint.end(PreserveTokens)
        }
    }
    return this
}

private fun Token.sameAsOffset(offset: Offset) =
    type == BaseTokenType.NewLine && text.indentLength() == offset.size

private fun <T> T.discardOffset(lexer: KSharpLexerIterator, offset: Offset): T {
    val lookAhead = lexer.state.lookAHeadState.checkpoint()
    lexer.state.value.indentationOffset.remove(offset)
    if (lexer.hasNext()) {
        val token = lexer.next()
        val action = if (token.sameAsOffset(offset)) {
            ConsumeTokens
        } else PreserveTokens
        lookAhead.end(action)
    }
    return this
}

fun KSharpConsumeResult.addIndentationOffset(
    indentationType: IndentationOffsetType,
    type: OffsetType
): KSharpConsumeResult =
    map {
        it.tokens.addIndentationOffset(indentationType, type)
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
                val indentLength = token.text.indentLength()
                lexerState.lineOffset.add(token.newLineStartOffset)
                val result = indentationOffset.update(indentLength)
                if (result == OffsetAction.Same && indentLength != 0) continue
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
            }.discardOffset(lexer, offset)
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
            .discardOffset(lexer, offset)
    }

fun <T> KSharpLexerIterator.withNextTokenIndentationOffset(
    type: OffsetType,
    block: (KSharpLexerIterator) -> T
): T {
    val lexerState = state.value
    val indentationOffset = lexerState.indentationOffset
    addIndentationOffset(IndentationOffsetType.NextToken, type)
    val currentOffset = indentationOffset.currentOffset
    return block(this).discardOffset(this, currentOffset)
}

fun <T> KSharpLexerIterator.withIndentationOffset(
    indentationType: IndentationOffsetType,
    type: OffsetType,
    block: (KSharpLexerIterator) -> T
): T {
    val indentationOffset = state.value.indentationOffset
    addIndentationOffset(indentationType, type)
    val currentOffset = indentationOffset.currentOffset
    return block(this).discardOffset(this, currentOffset)
}

fun KSharpConsumeResult.thenWithIndentationOffset(
    indentationType: IndentationOffsetType,
    type: OffsetType,
    block: (KSharpConsumeResult) -> KSharpConsumeResult
): KSharpConsumeResult = flatMap {
    it.tokens.withIndentationOffset(indentationType, type) { _ ->
        block(Either.Right(it))
    }
}
