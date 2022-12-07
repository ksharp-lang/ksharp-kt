package ksharp.parser

import org.ksharp.common.annotation.Mutable
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

internal class ConsIterator internal constructor(
    private val token: LexerToken,
    internal val iterator: Iterator<LexerToken>
) : Iterator<LexerToken> {

    internal var tokenConsumed = false
        private set

    override fun hasNext(): Boolean {
        if (!tokenConsumed) {
            return true
        }
        return iterator.hasNext()
    }

    override fun next(): LexerToken {
        if (!tokenConsumed) {
            tokenConsumed = true
            return token
        }
        return iterator.next()
    }

}

fun Iterator<LexerToken>.cons(token: LexerToken): Iterator<LexerToken> {
    if (this is ConsIterator && tokenConsumed) {
        return ConsIterator(token, iterator)
    }
    return ConsIterator(token, this)
}
