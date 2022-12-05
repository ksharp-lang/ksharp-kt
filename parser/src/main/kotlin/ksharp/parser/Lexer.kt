package ksharp.parser

import org.ksharp.common.annotation.Mutable
import java.io.Reader

typealias TokenFactory = Lexer.(c: Char) -> LexerToken?

interface TokenType

enum class BaseTokenType : TokenType {
    Unknown
}

interface LexerToken {
    val type: TokenType
    val text: String
    val startOffset: Int
    val endOffset: Int
}

class LToken internal constructor(
    override val type: TokenType,
    private val token: TextToken
) : LexerToken {
    override val text: String
        get() = token.text

    override val startOffset: Int
        get() = token.startOffset

    override val endOffset: Int
        get() = token.endOffset

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LexerToken

        if (type != other.type) return false
        if (text != other.text) return false
        if (startOffset != other.startOffset) return false
        if (endOffset != other.endOffset) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + startOffset
        result = 31 * result + endOffset
        return result
    }
}

@Mutable
class Lexer internal constructor(
    private val stream: CharStream,
    private val factory: TokenFactory
) {

    fun token(type: TokenType, skip: Int): LexerToken = LToken(type, stream.token(skip)!!)

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