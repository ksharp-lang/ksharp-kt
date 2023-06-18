package org.ksharp.parser

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.atomic.AtomicInteger

private enum class TempTokens : TokenType {
    TOKEN
}

private data class TempToken(override val text: String, override val type: TokenType) : Token {
    override fun collapse(newType: TokenType, text: String, end: Token): Token {
        TODO("Not required for tests")
    }

    override fun new(type: TokenType): Token {
        TODO("Not required for tests")
    }

    override val endOffset: Int
        get() = 0

    override val startOffset: Int
        get() = 0

}

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
    "Filter lexer iterator" {
        val stateVariable = LexerState(0)
        generateLexerIterator(stateVariable) {
            stateVariable.update(stateVariable.value.inc())
            TempToken(stateVariable.value.toString(), TempTokens.TOKEN)
        }.filter { it.text.toInt() % 2 == 0 }
            .apply {
                asSequence().take(3).toList().shouldBe(
                    listOf(
                        TempToken("2", TempTokens.TOKEN),
                        TempToken("4", TempTokens.TOKEN),
                        TempToken("6", TempTokens.TOKEN)
                    )
                )
            }
    }
    "Empty lexer iterator" {
        emptyLexerIterator<TempToken, String>(LexerState(""))
            .asSequence()
            .toList().shouldBe(emptyList())
    }
    "One lexer iterator" {
        oneLexerIterator(LexerState(""), TempToken("Hello", TempTokens.TOKEN))
            .asSequence()
            .toList()
            .shouldBe(listOf(TempToken("Hello", TempTokens.TOKEN)))
    }
    "Map lexer iterator" {
        val stateVariable = LexerState(0)
        generateLexerIterator(stateVariable) {
            stateVariable.update(stateVariable.value.inc())
            TempToken(stateVariable.value.toString(), TempTokens.TOKEN)
        }.map {
            TempToken("tk - ${it.text}", TempTokens.TOKEN)
        }.asSequence()
            .take(2)
            .toList()
            .shouldBe(listOf(TempToken("tk - 1", TempTokens.TOKEN), TempToken("tk - 2", TempTokens.TOKEN)))
    }
    "Lookahead Iterator generator test" {
        val counter = AtomicInteger(0)
        generateLexerIterator(LexerState("")) {
            LexerToken(BaseTokenType.Unknown, TextToken(counter.incrementAndGet().toString(), 0, 0))
        }.enableLookAhead()
            .generateIteratorWithLookAhead {
                var firstResult: Token? = null
                if (hasNext()) {
                    firstResult = next()
                }
                hasNext()
                lookNext()
                firstResult
            }.asSequence()
            .take(3)
            .toList()
            .shouldBe(
                listOf(
                    LexerToken(BaseTokenType.Unknown, TextToken("1", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("2", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("3", 0, 0)),
                )
            )
    }
    "Lookahead Iterator generator, clearing buffer test" {
        val counter = AtomicInteger(0)
        generateLexerIterator(LexerState("")) {
            LexerToken(BaseTokenType.Unknown, TextToken(counter.incrementAndGet().toString(), 0, 0))
        }.enableLookAhead()
            .generateIteratorWithLookAhead {
                var firstResult: Token? = null
                if (hasNext()) {
                    firstResult = next()
                }
                hasNext()
                lookNext()
                clearBuffer()
                firstResult
            }.asSequence()
            .take(3)
            .toList()
            .shouldBe(
                listOf(
                    LexerToken(BaseTokenType.Unknown, TextToken("1", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("3", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("5", 0, 0)),
                )
            )
    }
})