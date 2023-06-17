package org.ksharp.parser

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.ErrorCode
import org.ksharp.common.new
import org.ksharp.parser.ksharp.enableDiscardBlockAndNewLineTokens
import org.ksharp.parser.ksharp.lexerModule
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import java.util.concurrent.atomic.AtomicInteger

private fun <S> BaseLexerIterator<S>.consume(size: Int) {
    repeat((0 until size).count()) {
        if (this.hasNext()) {
            this.next()
        }
    }
}

enum class LookAHeadError(override val description: String) : ErrorCode {
    Error1("Error 1")
}

class LookAHeadTest : StringSpec({
    "Given a look a head lexer, if an error should reset" {
        val counter = AtomicInteger(0)
        generateLexerIterator(LexerState("")) {
            LexerToken(BaseTokenType.Unknown, TextToken(counter.incrementAndGet().toString(), 0, 0))
        }.enableLookAhead().lookAHead {
            it.consume(3)
            LookAHeadError.Error1.new().asLookAHeadResult()
        }.also {
            it.shouldBeLeft()
        }.remainTokens
            .asSequence()
            .take(4)
            .toList()
            .shouldBe(
                listOf(
                    LexerToken(BaseTokenType.Unknown, TextToken("1", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("2", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("3", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("4", 0, 0))
                )
            )
    }
    "Given a look a head lexer, if a value is produced should discard tokens" {
        val counter = AtomicInteger(0)
        generateLexerIterator(LexerState("")) {
            LexerToken(BaseTokenType.Unknown, TextToken(counter.incrementAndGet().toString(), 0, 0))
        }.enableLookAhead()
            .lookAHead {
                it.consume(3)
                10.asLookAHeadResult()
            }.also {
                it.shouldBeRight()
            }.remainTokens
            .asSequence()
            .take(4)
            .toList()
            .shouldBe(
                listOf(
                    LexerToken(BaseTokenType.Unknown, TextToken("4", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("5", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("6", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("7", 0, 0))
                )
            )
    }
    "Nested look a head" {
        val counter = AtomicInteger(0)
        generateLexerIterator(LexerState("")) {
            LexerToken(BaseTokenType.Unknown, TextToken(counter.incrementAndGet().toString(), 0, 0))
        }.enableLookAhead()
            .lookAHead { l ->
                l.lookAHead {
                    it.consume(3)
                    10.asLookAHeadResult()
                }.also {
                    it.shouldBeRight()
                }.remainTokens
                    .asSequence()
                    .take(4)
                    .toList()
                    .shouldBe(
                        listOf(
                            LexerToken(BaseTokenType.Unknown, TextToken("4", 0, 0)),
                            LexerToken(BaseTokenType.Unknown, TextToken("5", 0, 0)),
                            LexerToken(BaseTokenType.Unknown, TextToken("6", 0, 0)),
                            LexerToken(BaseTokenType.Unknown, TextToken("7", 0, 0))
                        )
                    )
                LookAHeadError.Error1.new().asLookAHeadResult()
            }.remainTokens
            .asSequence()
            .take(4)
            .toList()
            .shouldBe(
                listOf(
                    LexerToken(BaseTokenType.Unknown, TextToken("1", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("2", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("3", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("4", 0, 0))
                )
            )
    }
    "KSharp Look ahead" {
        "sum 10 20"
            .lexerModule(false)
            .enableDiscardBlockAndNewLineTokens {
                it.lookAHead { l ->
                    l.consume(1)
                    10.asLookAHeadResult()
                }.remainTokens
                    .asSequence()
                    .take(2)
                    .map(Token::text)
                    .toList()
                    .also(::println)
                    .shouldBe(
                        listOf(
                            "10",
                            "20"
                        )
                    )
            }
    }
})