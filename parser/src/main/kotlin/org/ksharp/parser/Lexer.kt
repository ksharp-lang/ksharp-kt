package org.ksharp.parser

import org.ksharp.common.Line
import org.ksharp.common.Offset
import org.ksharp.common.Position
import org.ksharp.common.annotation.Mutable
import org.ksharp.common.cast
import java.io.Reader

typealias TokenFactory<V> = Lexer<V>.(c: Char) -> LexerToken?
typealias TokenLexerIterator<S> = LexerIterator<LexerToken, S>
typealias BaseLexerIterator<S> = LexerIterator<out Token, S>

@Mutable
class Lexer<V> internal constructor(
    val state: LexerState<V>,
    private val stream: CharStream,
    private val factory: TokenFactory<V>
) {
    fun token(type: TokenType, skip: Int) = LexerToken(type, stream.token(skip)!!)
    fun nextChar(): Char? = stream.read()
    fun next(): LexerToken? =
        nextChar()?.let { c ->
            factory(c) ?: run {
                token(BaseTokenType.Unknown, 0)
            }
        }
}

fun <V> lexer(initialState: V, content: CharStream, factory: TokenFactory<V>): TokenLexerIterator<V> {
    val state = LexerState(initialState)
    val l = Lexer(state, content, factory)
    return generateLexerIterator(state) {
        l.next()
    }.cast()
}

fun <V> String.lexer(initialState: V, factory: TokenFactory<V>) = lexer(initialState, charStream(), factory)
fun <V> Reader.lexer(initialState: V, factory: TokenFactory<V>) = lexer(initialState, charStream(), factory)

/**
 * Use this method before enable lookAhead, to reduce the amount of tokens produced by collapsing the tokens with the same type
 */
fun <V> BaseLexerIterator<V>.collapseTokens(vararg excludeTokens: TokenType): BaseLexerIterator<V> {
    val tokenTypes = excludeTokens.toSet()
    var lastToken: Token? = null
    return generateLexerIterator(state) {
        var token: Token? = lastToken
        lastToken = null
        while (hasNext()) {
            if (token == null) {
                token = next()
                continue
            }
            lastToken = next()
            if (token.type == lastToken!!.type && !tokenTypes.contains(token.type)) {
                token = token.collapse(
                    token.type,
                    "${token.text}${lastToken!!.text}",
                    lastToken!!
                )
                continue
            }
            break
        }
        token
    }
}

fun <S> BaseLexerIterator<S>.filterWhiteSpace(): BaseLexerIterator<S> = filter {
    it.type != BaseTokenType.WhiteSpace
}

fun <V> TokenLexerIterator<V>.toLogicalLexerToken(): BaseLexerIterator<V> =
    object : LexerIterator<Token, V> {
        private var startPosition: Position = Line(1) to Offset(0)
        private var lineOffset: Int = 0

        override val lastEndOffset: Int get() = this@toLogicalLexerToken.lastEndOffset

        override val lastStartOffset: Int get() = this@toLogicalLexerToken.lastStartOffset

        override val state: LexerState<V> = this@toLogicalLexerToken.state

        override fun hasNext(): Boolean = this@toLogicalLexerToken.hasNext()

        override fun next(): LogicalLexerToken = with(this@toLogicalLexerToken.next()) {
            if (type == BaseTokenType.NewLine || type == BaseTokenType.IgnoreNewLine) {
                startPosition = Line(startPosition.first.value.inc()) to Offset(0)
                lineOffset = startOffset + (if (text.startsWith("\r\n")) 2 else 1)
            } else {
                startPosition = startPosition.first to Offset(startOffset - lineOffset)
            }
            LogicalLexerToken(
                this,
                startPosition = startPosition,
                endPosition = startPosition.first to Offset(startPosition.second.value + text.length)
            )
        }
    }


internal fun String.indentLength() =
    replace("\n", "").replace("\r", "") //normalize newline to zero spaces
        .replace("\t", "  ") //normalize tab to two spaces
        .length

fun <V> BaseLexerIterator<V>.excludeIgnoreNewLineTokens(): BaseLexerIterator<V> = filter {
    it.type != BaseTokenType.IgnoreNewLine
}

fun <V> BaseLexerIterator<V>.collapseNewLines(): BaseLexerIterator<V> {
    var lastIndent = 0
    return generateLexerIterator(state) {
        while (hasNext()) {
            val token = next()
            lastIndent = if (token.type == BaseTokenType.NewLine) {
                val length = token.text.indentLength()
                if (length == lastIndent) continue
                else length
            } else -1
            return@generateLexerIterator token
        }
        null
    }
}
