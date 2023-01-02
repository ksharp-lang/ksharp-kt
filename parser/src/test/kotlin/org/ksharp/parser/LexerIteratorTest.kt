package org.ksharp.parser

import io.kotest.assertions.throwables.shouldThrow
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

private fun LexerIterator<TempToken, Int>.nextIterator(): TempToken =
    if (hasNext()) next() else throw NoSuchElementException()

class LexerIteratorTest : StringSpec({
    "Check call next before hasNext" {
        val stateVariable = LexerState(0)
        generateLexerIterator(stateVariable) {
            stateVariable.update(stateVariable.value.inc())
            TempToken(stateVariable.value.toString(), TempTokens.TOKEN)
        }.apply {
            shouldThrow<NoSuchElementException> {
                next()
            }
        }
    }
    "Generate a lexer iterator" {
        val stateVariable = LexerState(0)
        generateLexerIterator(stateVariable) {
            stateVariable.update(stateVariable.value.inc())
            TempToken(stateVariable.value.toString(), TempTokens.TOKEN)
        }.apply {
            asSequence()
                .take(5)
                .toList().shouldBe(
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
    "Generate a finite lexer iterator" {
        val state = LexerState(0)
        generateLexerIterator(state) {
            state.update(state.value.inc())
            if (state.value <= 3) {
                TempToken(state.value.toString(), TempTokens.TOKEN)
            } else null
        }.asSequence()
            .apply {
                toList().shouldBe(
                    listOf(
                        TempToken("1", TempTokens.TOKEN),
                        TempToken("2", TempTokens.TOKEN),
                        TempToken("3", TempTokens.TOKEN)
                    )
                )
                state.value.shouldBe(4)
            }
    }
    "Check cons, next and cons again" {
        val stateVariable = LexerState(0)
        generateLexerIterator(stateVariable) {
            stateVariable.update(stateVariable.value.inc())
            if (stateVariable.value <= 3) {
                TempToken(stateVariable.value.toString(), TempTokens.TOKEN)
            } else null
        }.cons(TempToken("99", TempTokens.TOKEN))
            .apply {
                state.value.shouldBe(0)
                nextIterator().shouldBe(TempToken("99", TempTokens.TOKEN))
                nextIterator().shouldBe(TempToken("1", TempTokens.TOKEN))
            }.cons(TempToken("50", TempTokens.TOKEN))
            .apply {
                asSequence().toList().shouldBe(
                    listOf(
                        TempToken("50", TempTokens.TOKEN),
                        TempToken("2", TempTokens.TOKEN),
                        TempToken("3", TempTokens.TOKEN)
                    )
                )
                state.value.shouldBe(4)
            }
    }
})