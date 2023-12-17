package org.ksharp.lsp.capabilities.actions

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import org.ksharp.lsp.actions.ActionExecutionState
import org.ksharp.lsp.actions.ParseAction
import org.ksharp.lsp.actions.SemanticTokensAction
import org.ksharp.lsp.actions.documentActions

private fun String.spec(document: String, expectedTokens: String) =
    this to (document to
            expectedTokens.splitToSequence(",")
                .map { it.trim().toInt() }.toList())

private val specs = listOf(
    "Invalid tokens".spec("1 + 2", "0, 0, 1, 5, 0, 0, 2, 1, 4, 0, 0, 2, 1, 5, 0"),
    "Invalid tokens 2".spec(
        "\"Hello\" n->str True : + / . \$ ! | ^ &  =! < 0xABC 0b001 0o2341 10 1.5 var let if then as type native pub internal @",
        "0, 0, 7, 1, 0, 0, 8, 6, 2, 0, 0, 7, 4, 0, 0, 0, 5, 1, 4, 0, 0, 2, 1, 4, 0, 0, 2, 1, 4, 0, 0, 2, 1, 4, 0, 0, 2, 1, 4, 0, 0, 2, 1, 4, 0, 0, 2, 1, 4, 0, 0, 2, 1, 4, 0, 0, 2, 1, 4, 0, 0, 3, 2, 4, 0, 0, 3, 1, 4, 0, 0, 2, 5, 5, 0, 0, 6, 5, 5, 0, 0, 6, 6, 5, 0, 0, 7, 2, 5, 0, 0, 3, 3, 5, 0, 0, 8, 3, 6, 0, 0, 4, 2, 6, 0, 0, 8, 2, 6, 0, 0, 3, 4, 6, 0, 0, 5, 6, 6, 0, 0, 7, 3, 6, 0, 0, 4, 8, 6, 0, 0, 9, 1, 4, 0"
    ),
    "Import".spec(
        "import math as m",
        "0, 0, 6, 6, 0, 0, 7, 4, 7, 0, 0, 5, 2, 6, 0, 0, 3, 1, 7, 0"
    ),
    "Type annotations".spec(
        """
          @native("Test")
          type Num = Int
      """.trimIndent(),
        "0, 0, 7, 12, 0, 0, 8, 6, 1, 0, 0, -8, 7, 12, 0, 0, 8, 6, 1, 0, 1, 0, 4, 6, 0, 0, 5, 3, 0, 1, 0, 4, 1, 4, 0, 0, 2, 3, 3, 0"
    ),
    "Internal types".spec(
        "internal type Integer = Int",
        "0, 0, 8, 6, 0, 0, 9, 4, 6, 0, 0, 5, 7, 0, 1, 0, 8, 1, 4, 0, 0, 2, 3, 3, 0"
    ),
    "Parametric tokens".spec(
        "type Map a b = Map a b",
        "0, 0, 4, 6, 0, 0, 5, 3, 0, 1, 0, 4, 1, 9, 0, 0, 2, 1, 9, 0, 0, 2, 1, 4, 0, 0, 2, 3, 3, 0, 0, 4, 1, 10, 0, 0, 2, 1, 10, 0"
    ),
    "Union types".spec(
        "type Bool = True | False",
        "0, 0, 4, 6, 0, 0, 5, 4, 0, 1, 0, 5, 1, 4, 0, 0, 2, 4, 3, 0, 0, 5, 1, 4, 0, 0, 2, 5, 3, 0"
    ),
    "Intersected types".spec(
        "type Bool = True & False",
        "0, 0, 4, 6, 0, 0, 5, 4, 0, 1, 0, 5, 1, 4, 0, 0, 2, 4, 3, 0, 0, 5, 1, 4, 0, 0, 2, 5, 3, 0"
    ),
    "Tuple types".spec(
        "type Point = x: Int, y: Int",
        "0, 0, 4, 6, 0, 0, 5, 5, 0, 1, 0, 6, 1, 4, 0, 0, 2, 2, 11, 0, 0, 3, 3, 3, 0, 0, 3, 1, 4, 0, 0, 2, 2, 11, 0, 0, 3, 3, 3, 0"
    ),
    "Function types".spec(
        "type Sum a b = a -> b -> Int",
        "0, 0, 4, 6, 0, 0, 5, 3, 0, 1, 0, 4, 1, 9, 0, 0, 2, 1, 9, 0, 0, 2, 1, 4, 0, 0, 2, 1, 10, 0, 0, 2, 2, 4, 0, 0, 3, 1, 10, 0, 0, 2, 2, 4, 0, 0, 3, 3, 3, 0"
    ),
    "Internal type".spec(
        "internal type StringMap a = Map String a",
        "0, 0, 8, 6, 0, 0, 9, 4, 6, 0, 0, 5, 9, 0, 1, 0, 10, 1, 9, 0, 0, 2, 1, 4, 0, 0, 2, 3, 3, 0, 0, 4, 6, 3, 0, 0, 7, 1, 10, 0"
    ),
    "Type declarations".spec(
        "sum a b :: a -> b -> Int",
        "0, 0, 3, 8, 0, 0, 8, 2, 4, 0, 0, -4, 1, 10, 0, 0, 2, 1, 10, 0, 0, 5, 1, 10, 0, 0, 2, 2, 4, 0, 0, 3, 1, 10, 0, 0, 2, 2, 4, 0, 0, 3, 3, 3, 0"
    ),
    "annotation type declarations".spec(
        """
            @native("Hello World")
            sum a b :: a -> b -> Int
        """.trimIndent(),
        "0, 0, 7, 12, 0, 0, 8, 13, 1, 0, 0, -8, 7, 12, 0, 0, 8, 13, 1, 0, 1, 0, 3, 8, 0, 0, 8, 2, 4, 0, 0, -4, 1, 10, 0, 0, 2, 1, 10, 0, 0, 5, 1, 10, 0, 0, 2, 2, 4, 0, 0, 3, 1, 10, 0, 0, 2, 2, 4, 0, 0, 3, 3, 3, 0"
    ),
    "function".spec(
        "sum a b = a + b",
        "0, 0, 3, 8, 0, 0, 4, 1, 10, 0, 0, 2, 1, 10, 0, 0, 2, 1, 4, 0, 0, 4, 1, 4, 0"
    ),
    "native functions".spec(
        "native sum a b",
        "0, 0, 6, 6, 0, 0, 7, 3, 8, 0, 0, 4, 1, 10, 0, 0, 2, 1, 10, 0"
    ),
    "native pub functions".spec(
        "native pub sum a b",
        "0, 0, 6, 6, 0, 0, 7, 3, 6, 0, 0, 4, 3, 8, 0, 0, 4, 1, 10, 0, 0, 2, 1, 10, 0"
    ),
    "pub functions".spec(
        "pub sum a b = a + b",
        "0, 0, 3, 6, 0, 0, 4, 3, 8, 0, 0, 4, 1, 10, 0, 0, 2, 1, 10, 0, 0, 2, 1, 4, 0, 0, 4, 1, 4, 0"
    ),
    "if, number and string".spec(
        "sum a b = if a < 10 then True else \"Hello World\"",
        "0, 0, 3, 8, 0, 0, 4, 1, 10, 0, 0, 2, 1, 10, 0, 0, 2, 1, 4, 0, 0, 2, 2, 4, 0, 0, 5, 1, 4, 0, 0, 2, 2, 5, 0, 0, 3, 4, 4, 0, 0, -5, 1, 4, 0, 0, 2, 2, 5, 0, 0, 13, 4, 4, 0, 0, 5, 13, 1, 0"
    ),
    "if without else".spec(
        "ifFn = if True then False",
        "0, 0, 4, 8, 0, 0, 5, 1, 4, 0, 0, 2, 2, 4, 0, 0, 8, 4, 4, 0"
    ),
    "let".spec(
        """
            sum = let x = 10
                      y = 20
                  then x + y
        """.trimIndent(),
        "0, 0, 3, 8, 0, 0, 4, 1, 4, 0, 0, 2, 3, 4, 0, 0, 6, 1, 4, 0, 0, 2, 2, 5, 0, 1, 12, 1, 4, 0, 0, 2, 2, 5, 0, 1, 6, 4, 4, 0, 0, 7, 1, 4, 0"
    ),
    "function annotations".spec(
        """
            @native("Test")
            sum a b = a + b
        """.trimIndent(),
        "0, 0, 7, 12, 0, 0, 8, 6, 1, 0, 0, -8, 7, 12, 0, 0, 8, 6, 1, 0, 1, 0, 3, 8, 0, 0, 4, 1, 10, 0, 0, 2, 1, 10, 0, 0, 2, 1, 4, 0, 0, 4, 1, 4, 0"
    ),
    "function annotations 2".spec(
        """
            |@native(lang=["java", "c#"])
            |var = 10
        """.trimMargin("|")
            .replace("\r", ""),
        "0, 0, 7, 12, 0, 0, 8, 4, 10, 0, 0, 4, 1, 4, 0, 0, 2, 6, 1, 0, 0, 8, 4, 1, 0, 0, -22, 7, 12, 0, 0, 8, 4, 10, 0, 0, 4, 1, 4, 0, 0, 2, 6, 1, 0, 0, 8, 4, 1, 0, 1, 0, 3, 8, 0, 0, 4, 1, 4, 0, 0, 2, 2, 5, 0"
    ),
    "list".spec(
        "list = [10, 20, 30]",
        "0, 0, 4, 8, 0, 0, 5, 1, 4, 0, 0, 3, 2, 5, 0, 0, 4, 2, 5, 0, 0, 4, 2, 5, 0"
    ),
    "map".spec(
        "map = {\"key1\": 10}",
        "0, 0, 3, 8, 0, 0, 4, 1, 4, 0, 0, 3, 6, 1, 0, 0, 6, 1, 4, 0, 0, 2, 2, 5, 0"
    ),
    "function call".spec(
        "sumTwo a = sum a 2",
        "0, 0, 6, 8, 0, 0, 7, 1, 10, 0, 0, 2, 1, 4, 0, 0, 2, 3, 2, 0, 0, 6, 1, 5, 0"
    ),
    "Impl".spec(
        """
            impl Pow for Num =
                native (**) a b
        """.trimIndent(),
        "0, 0, 4, 6, 0, 0, 5, 3, 0, 1, 0, 4, 3, 6, 0, 0, 4, 3, 3, 0, 0, 4, 1, 4, 0, 1, 4, 6, 6, 0, 0, 7, 4, 8, 0, 0, 5, 1, 10, 0, 0, 2, 1, 10, 0"
    )
)


class CalculateSemanticTokensTest : FreeSpec({
    "Calculate semantic tokens " - {
        specs.forEach { (desc, spec) ->
            desc {
                val actions = documentActions("doc")
                val state = ActionExecutionState()
                actions(state, ParseAction, spec.first)
                state[SemanticTokensAction].get()
                    .shouldBe(spec.second)
            }
        }
    }
})
