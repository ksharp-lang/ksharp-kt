package ksharp.parser

import org.ksharp.common.Position

interface TokenType

enum class BaseTokenType : TokenType {
    Unknown
}

data class LexerToken internal constructor(
    val type: TokenType,
    private val token: TextToken
) {
    val text: String = token.text
    val startPosition: Position = token.start
    val endPosition: Position = token.end
}
