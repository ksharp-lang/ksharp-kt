package org.ksharp.parser

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset
import org.ksharp.parser.ksharp.KSharpTokenType

class LexerTokenTest : StringSpec({
    "Assert LexerToken properties access" {
        val token = LexerToken(
            BaseTokenType.Unknown,
            TextToken("H", 2, 3)
        )
        token.type.shouldBe(BaseTokenType.Unknown)
        token.text.shouldBe("H")
        token.startOffset.shouldBe(2)
        token.endOffset.shouldBe(3)
        token.location.shouldBe(Location.NoProvided)
    }
    "Assert LogicalLexerToken properties access" {
        val token = LexerToken(
            BaseTokenType.Unknown,
            TextToken("H", 2, 3)
        )
        val logicalToken = LogicalLexerToken(
            token, "File", Line(1) to Offset(0), Line(2) to Offset(0)
        )
        logicalToken.type.shouldBe(BaseTokenType.Unknown)
        logicalToken.text.shouldBe("H")
        logicalToken.startOffset.shouldBe(2)
        logicalToken.endOffset.shouldBe(3)
        logicalToken.startPosition.shouldBe(Line(1) to Offset(0))
        logicalToken.endPosition.shouldBe(Line(2) to Offset(0))
        logicalToken.location.shouldBe(Location("File", Line(1) to Offset(0)))
    }
    "Check LexerToken collapse" {
        val token1 = LexerToken(
            BaseTokenType.Unknown,
            TextToken("H", 2, 3)
        )
        val token2 = LexerToken(
            BaseTokenType.Unknown,
            TextToken("H", 2, 10)
        )
        val collapsed = token1.collapse(KSharpTokenType.EndBlock, "J", token2) as LexerToken
        collapsed.type.shouldBe(KSharpTokenType.EndBlock)
        collapsed.text.shouldBe("J")
        collapsed.startOffset.shouldBe(2)
        collapsed.endOffset.shouldBe(10)
    }
    "Check LogicalLexerToken collapse" {
        val token1 = LexerToken(
            BaseTokenType.Unknown,
            TextToken("H", 2, 3)
        )
        val logicalToken1 = LogicalLexerToken(
            token1, "File", Line(1) to Offset(0), Line(2) to Offset(0)
        )
        val token2 = LexerToken(
            BaseTokenType.Unknown,
            TextToken("H", 2, 10)
        )
        val logicalToken2 = LogicalLexerToken(
            token2, "File", Line(3) to Offset(0), Line(4) to Offset(0)
        )
        val collapsed = logicalToken1.collapse(KSharpTokenType.EndBlock, "J", logicalToken2) as LogicalLexerToken
        collapsed.type.shouldBe(KSharpTokenType.EndBlock)
        collapsed.text.shouldBe("J")
        collapsed.startOffset.shouldBe(2)
        collapsed.endOffset.shouldBe(10)
        collapsed.startPosition.shouldBe(Line(1) to Offset(0))
        collapsed.endPosition.shouldBe(Line(4) to Offset(0))
    }
})