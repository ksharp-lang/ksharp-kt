package ksharp.parser

import org.ksharp.common.annotation.Mutable
import java.io.Reader

typealias TokenFactory = Lexer.(c: Char) -> LexerToken?

interface TokenType

enum class BaseTokenType : TokenType {
    Unknown
}

data class LexerToken internal constructor(
    val type: TokenType,
    private val token: TextToken
) {
    val text: String = token.text
    val startOffset: Int = token.startOffset
    val endOffset: Int = token.endOffset
}

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