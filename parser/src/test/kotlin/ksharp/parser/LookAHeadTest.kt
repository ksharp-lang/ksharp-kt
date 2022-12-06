package ksharp.parser

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import ksharp.test.shouldBeLeft
import ksharp.test.shouldBeRight
import org.ksharp.common.ErrorCode
import org.ksharp.common.new
import java.util.concurrent.atomic.AtomicInteger

fun Iterator<LexerToken>.consume(size: Int) {
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
        generateSequence {
            LexerToken(BaseTokenType.Unknown, TextToken(counter.incrementAndGet().toString(), 0, 0))
        }.iterator()
            .lookAHead {
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
        generateSequence {
            LexerToken(BaseTokenType.Unknown, TextToken(counter.incrementAndGet().toString(), 0, 0))
        }.iterator()
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
    "Given a look a head lexer, if a value is produced should discard tokens leaving the last tokens" {
        val counter = AtomicInteger(0)
        generateSequence {
            LexerToken(BaseTokenType.Unknown, TextToken(counter.incrementAndGet().toString(), 0, 0))
        }.iterator()
            .lookAHead {
                it.consume(3)
                10.asLookAHeadResult(2)
            }.also {
                it.shouldBeRight()
            }.remainTokens
            .asSequence()
            .take(4)
            .toList()
            .shouldBe(
                listOf(
                    LexerToken(BaseTokenType.Unknown, TextToken("2", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("3", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("4", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("5", 0, 0))
                )
            )
    }
})