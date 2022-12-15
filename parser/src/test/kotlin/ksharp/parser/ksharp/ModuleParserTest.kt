package ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec

class ModuleParserTest : StringSpec({
    "Parse moduleName" {
        "ksharp.math as math"
            .kSharpLexer()
            .moduleName()
            .also {

            }
    }
}
)