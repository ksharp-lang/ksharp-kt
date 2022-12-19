package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.nodes.ImportNode
import org.ksharp.parser.LexerToken
import org.ksharp.parser.TextToken
import org.ksharp.test.shouldBeRight

class ImportParserTest : StringSpec({
    "Parse moduleName" {
        "ksharp.math as math"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeModuleName()
            .map { it.value }
            .shouldBeRight("ksharp.math")
    }
    "Parse import" {
        "import ksharp.math as math"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions { LexerToken(KSharpTokenType.EndExpression, TextToken("", 0, 0)) }
            .consumeImport()
            .map { it.value }
            .shouldBeRight(ImportNode("ksharp.math", "math", Location.NoProvided))
    }
})