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


fun Iterator<LexerToken>.collapseTokens(): Iterator<LexerToken> {
    var token: LexerToken?
    var lastToken: LexerToken? = null

    return generateIterator {
        token = lastToken
        lastToken = null
        while (hasNext()) {
            lastToken = next()
            if (token == null) {
                token = lastToken
                continue
            }
            if (lastToken!!.type == token!!.type) {
                token = token!!.copy(
                    token = TextToken(
                        text = "${token!!.text}${lastToken!!.text}",
                        startOffset = token!!.startOffset,
                        endOffset = lastToken!!.endOffset
                    )
                )
                lastToken = null
                continue
            }
            break
        }
        token
    }
}

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