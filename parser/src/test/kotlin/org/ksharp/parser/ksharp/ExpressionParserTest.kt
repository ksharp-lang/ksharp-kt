package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.test.shouldBeRight

class ExpressionParserTest : StringSpec({
    "lowercase token function" {
        "sum 10 20"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpressionValue()
            .map { it.value }
            .shouldBeRight()
    }
})