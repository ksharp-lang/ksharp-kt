package org.ksharp.parser

import org.ksharp.common.Line
import org.ksharp.common.Offset
import org.ksharp.common.Position
import org.ksharp.common.annotation.Mutable
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
    }
}

fun <V> String.lexer(initialState: V, factory: TokenFactory<V>) = lexer(initialState, charStream(), factory)
fun <V> Reader.lexer(initialState: V, factory: TokenFactory<V>) = lexer(initialState, charStream(), factory)

fun <V> BaseLexerIterator<V>.collapseTokens(canCollapse: (TokenType) -> Boolean = { true }): BaseLexerIterator<V> {
    var token: Token?
    var lastToken: Token? = null

    return generateLexerIterator(state) {
        token = lastToken
        lastToken = null
        while (hasNext()) {
            lastToken = next()
            if (token == null) {
                token = lastToken
                continue
            }
            if (token!!.type == lastToken!!.type && canCollapse(token!!.type)) {
                token = token!!.collapse(
                    token!!.type,
                    "${token!!.text}${lastToken!!.text}",
                    lastToken!!
                )
                lastToken = null
                continue
            }
            break
        }
        token
    }
}

fun <V> TokenLexerIterator<V>.toLogicalLexerToken(
    context: String,
    newLineType: TokenType
): BaseLexerIterator<V> =
    object : LexerIterator<Token, V> {
        private var startPosition: Position = Line(1) to Offset(0)
        private var startLineOffset: Int = 0

        override val state: LexerState<V> = this@toLogicalLexerToken.state

        override fun hasNext(): Boolean = this@toLogicalLexerToken.hasNext()

        override fun next(): LogicalLexerToken = with(this@toLogicalLexerToken.next()) {
            if (type == newLineType) {
                startPosition = Line(startPosition.first.value.inc()) to Offset(0)
                startLineOffset = endOffset.inc()
            }
            LogicalLexerToken(
                this,
                context,
                startPosition = startPosition,
                endPosition = startPosition.first to Offset((endOffset - startLineOffset).coerceAtLeast(0))
            )
        }
    }