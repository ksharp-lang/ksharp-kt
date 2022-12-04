package ksharp.parser

import org.ksharp.common.annotation.Mutable
import java.io.Reader

typealias TokenFactory = Lexer.(c: Char) -> LexerToken?

interface TokenType

enum class BaseTokenType : TokenType {
    Unknown
}

data class LexerToken(
    val type: TokenType,
    val text: TextToken
)

@Mutable
class Lexer internal constructor(
    private val stream: CharStream,
    private val factory: TokenFactory
) {

    fun token(type: TokenType, skip: Int) = stream.token(skip).let {
        LexerToken(type, it!!)
    }

    fun nextChar(): Char? = stream.read()

    fun next(): LexerToken? =
        nextChar()?.let { c ->
            factory(c) ?: run {
                token(BaseTokenType.Unknown, 0)
            }
        }

}

fun lexer(content: CharStream, factory: TokenFactory): Sequence<LexerToken> {
    val l = Lexer(content, factory)
    return generateSequence {
        l.next()
    }
}

fun String.lexer(factory: TokenFactory) = lexer(charStream(), factory)

fun Reader.lexer(factory: TokenFactory) = lexer(charStream(), factory)