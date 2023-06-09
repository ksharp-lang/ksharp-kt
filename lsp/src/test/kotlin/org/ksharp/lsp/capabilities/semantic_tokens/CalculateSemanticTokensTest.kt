package org.ksharp.lsp.capabilities.semantic_tokens

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class CalculateSemanticTokensTest : StringSpec({
    "invalid tokens" {
        calculateSemanticTokens("1 + 2")
            .shouldBe(listOf(0, 0, 1, 5, 0, 0, 2, 1, 4, 0, 0, 2, 1, 5, 0))
    }
    "parametric type token" {
        calculateSemanticTokens("type Map a b = Map a b")
            .shouldBe(
                listOf(
                    0,
                    0,
                    4,
                    6,
                    0,
                    0,
                    5,
                    3,
                    0,
                    1,
                    0,
                    4,
                    1,
                    9,
                    0,
                    0,
                    2,
                    1,
                    9,
                    0,
                    0,
                    2,
                    1,
                    4,
                    0,
                    0,
                    2,
                    3,
                    3,
                    0,
                    0,
                    4,
                    1,
                    10,
                    0,
                    0,
                    2,
                    1,
                    10,
                    0
                )
            )
    }
    "union type" {
        calculateSemanticTokens("type Bool = True | False")
            .shouldBe(listOf(0, 0, 4, 6, 0, 0, 5, 4, 0, 1, 0, 5, 1, 4, 0, 0, 2, 4, 3, 0, 0, 5, 1, 4, 0, 0, 2, 5, 3, 0))
    }
    "intersection type" {
        calculateSemanticTokens("type Bool = True & False")
            .shouldBe(listOf(0, 0, 4, 6, 0, 0, 5, 4, 0, 1, 0, 5, 1, 4, 0, 0, 2, 4, 3, 0, 0, 5, 1, 4, 0, 0, 2, 5, 3, 0))
    }
    "tuple type" {
        calculateSemanticTokens("type Bool = True, False")
            .shouldBe(listOf(0, 0, 4, 6, 0, 0, 5, 4, 0, 1, 0, 5, 1, 4, 0, 0, 2, 4, 3, 0, 0, 4, 1, 4, 0, 0, 2, 5, 3, 0))
    }
})
