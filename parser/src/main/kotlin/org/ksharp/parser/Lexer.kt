package org.ksharp.parser

import org.ksharp.common.Line
import org.ksharp.common.Offset
import org.ksharp.common.Position
import org.ksharp.common.annotation.Mutable
import org.ksharp.common.generateIterator
import java.io.Reader

typealias TokenFactory = Lexer.(c: Char) -> LexerToken?

@Mutable
class Lexer internal constructor(
    private val stream: CharStream,
    private val factory: TokenFactory
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

fun lexer(content: CharStream, factory: TokenFactory): Iterator<LexerToken> {
    val l = Lexer(content, factory)
    return generateSequence {
        l.next()
    }.iterator()
}

fun String.lexer(factory: TokenFactory) = lexer(charStream(), factory)
fun Reader.lexer(factory: TokenFactory) = lexer(charStream(), factory)

internal class ConsIterator<L : LexerValue> internal constructor(
    private val token: L,
    internal val iterator: Iterator<L>
) : Iterator<L> {

    internal var tokenConsumed = false
        private set

    override fun hasNext(): Boolean {
        if (!tokenConsumed) {
            return true
        }
        return iterator.hasNext()
    }

    override fun next(): L {
        if (!tokenConsumed) {
            tokenConsumed = true
            return token
        }
        return iterator.next()
    }

}

fun <L : LexerValue> Iterator<L>.cons(token: L): Iterator<L> {
    if (this is ConsIterator && tokenConsumed) {
        return ConsIterator(token, iterator)
    }
    return ConsIterator(token, this)
}

@Suppress("UNCHECKED_CAST")
/*
    When collapsing preserve the type of the end token
 */
inline fun <L : CollapsableToken> Iterator<L>.collapseTokens(
    crossinline predicate: (start: L, end: L) -> Boolean,
    crossinline collapse: (start: L, end: L) -> L = { start, end ->
        start.collapse(
            end.type,
            "${start.text}${end.text}",
            end
        ) as L
    }
): Iterator<L> {
    var token: L?
    var lastToken: L? = null

    return generateIterator {
        token = lastToken
        lastToken = null
        while (hasNext()) {
            lastToken = next()
            if (token == null) {
                token = lastToken
                continue
            }
            if (predicate(token!!, lastToken!!)) {
                token = collapse(token!!, lastToken!!)
                lastToken = null
                continue
            }
            break
        }
        token
    }
}

@Suppress("UNCHECKED_CAST")
fun <L : CollapsableToken> Iterator<L>.collapseTokens(): Iterator<L> = collapseTokens(predicate = { start, end ->
    start.type == end.type
})

fun Iterator<LexerToken>.toLogicalLexerToken(context: String, newLineType: TokenType): Iterator<LogicalLexerToken> =
    object : Iterator<LogicalLexerToken> {
        private var startPosition: Position = Line(1) to Offset(0)
        private var startLineOffset: Int = 0

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