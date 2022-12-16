package ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import ksharp.nodes.ImportNode
import ksharp.test.shouldBeRight
import org.ksharp.common.Location

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
            .consumeImport()
            .map { it.value }
            .shouldBeRight(ImportNode("ksharp.math", "math", Location.NoProvided))
    }
})