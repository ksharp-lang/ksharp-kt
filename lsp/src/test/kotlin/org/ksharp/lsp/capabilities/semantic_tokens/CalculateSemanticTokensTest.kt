package org.ksharp.lsp.capabilities.semantic_tokens

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

private fun String.spec(document: String, expectedTokens: String) =
    this to (document to
            expectedTokens.splitToSequence(",")
                .map { it.trim().toInt() }.toList())

private val specs = listOf(
    "Invalid tokens".spec("1 + 2", "0, 0, 1, 5, 0, 0, 2, 1, 4, 0, 0, 2, 1, 5, 0"),
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
    )
)

class CalculateSemanticTokensTest : FreeSpec({
    "Calculate semantic tokens " - {
        specs.forEach { (desc, spec) ->
            desc {
                calculateSemanticTokens(spec.first).shouldBe(spec.second)
            }
        }
    }
})
