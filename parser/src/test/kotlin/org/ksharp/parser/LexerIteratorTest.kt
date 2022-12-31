package org.ksharp.parser

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

private enum class TempTokens : TokenType {
    TOKEN
}

private data class TempToken(override val text: String, override val type: TokenType) : LexerValue, Token {
    override fun collapse(newType: TokenType, text: String, end: Token): Token {
        TODO("Not required for tests")
    }

}

class LexerIteratorTest : StringSpec({
    "Generate a lexer iterator" {
        val state = LexerState(0)
        generateLexerIterator(state) {
            state.update(state.value.inc())
            TempToken(state.value.toString(), TempTokens.TOKEN)
        }.asSequence()
            .take(5)
            .apply {
                toList().shouldBe(
                    listOf(
                        TempToken("1", TempTokens.TOKEN),
                        TempToken("2", TempTokens.TOKEN),
                        TempToken("3", TempTokens.TOKEN),
                        TempToken("4", TempTokens.TOKEN),
                        TempToken("5", TempTokens.TOKEN)
                    )
                )
                state.value.shouldBe(5)
            }
    }
    "Const a lexer iterator" {
        val state = LexerState(0)
        generateLexerIterator(state) {
            state.update(state.value.inc())
            TempToken(state.value.toString(), TempTokens.TOKEN)
        }.cons(TempToken("0", TempTokens.TOKEN))
            .asSequence()
            .take(5)
            .apply {
                toList().shouldBe(
                    listOf(
                        TempToken("0", TempTokens.TOKEN),
                        TempToken("1", TempTokens.TOKEN),
                        TempToken("2", TempTokens.TOKEN),
                        TempToken("3", TempTokens.TOKEN),
                        TempToken("4", TempTokens.TOKEN)
                    )
                )
                state.value.shouldBe(4)
            }
    }
})