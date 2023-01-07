package org.ksharp.parser.ksharp

import org.ksharp.common.annotation.Mutable
import org.ksharp.parser.Token
import org.ksharp.parser.TokenType

@Mutable
class BlockController(private val builder: (TokenType) -> Token) {
    private val register = ArrayDeque<Int>()
    private val pendingToken = ArrayDeque<Token>()
    private var ended = false
    private fun <T> ArrayDeque<T>.push(level: T) = addLast(level)
    private fun Token.whenNewLine(block: (Token) -> Token): Token =
        if (type == KSharpTokenType.NewLine) {
            block(this)
        } else this

    private fun addEndBlockTokens(untilLevel: Int) {
        while (true) {
            val level = register.removeLast()
            if (level == untilLevel) {
                pendingToken.push(builder(KSharpTokenType.NewLine))
                if (untilLevel != 1) {
                    register.push(level)
                } else pendingToken.push(builder(KSharpTokenType.EndBlock))
                return
            }
            pendingToken.push(builder(KSharpTokenType.NewLine))
            pendingToken.push(builder(KSharpTokenType.EndBlock))
        }
    }

    private fun Token.processNewLine(
        len: Int,
    ): Token {
        val level = register.last()

        //root level guard
        if (level == 1 && len == 1) {
            addEndBlockTokens(len)
            return pendingToken.removeFirst()
        }

        if (level == len) {
            return this
        }

        if (level > len) {
            addEndBlockTokens(len)
            return pendingToken.removeFirst()
        }

        register.push(len)
        return builder(KSharpTokenType.BeginBlock)
    }

    fun processToken(token: Token): Token {
        if (register.isEmpty()) {
            pendingToken.push(token)
            register.push(1)
            return builder(KSharpTokenType.BeginBlock)
        }
        return token
            .whenNewLine {
                it.processNewLine(
                    it.text.indentLength()
                )
            }
    }

    fun pendingToken(): Token? {
        if (!ended) return pendingToken.removeFirstOrNull()?.takeIf {
            it.type != KSharpTokenType.Ignore
        }
        while (true) {
            val item = pendingToken.removeFirstOrNull() ?: return null
            if (item.type == KSharpTokenType.Ignore) continue
            if (item.type == KSharpTokenType.BeginBlock) continue
            return item
        }
    }

    fun end(): Token? {
        ended = true
        return pendingToken()
    }

}