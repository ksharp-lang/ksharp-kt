package org.ksharp.module.prelude

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.module.FunctionInfo

private val FunctionInfo.representation: String
    get() = "$name :: ${
        types.joinToString(" -> ") {
            it.representation
        }
    }"

class PreludeModuleTest : StringSpec({
    "Test prelude module" {
        preludeModule.functions.values
            .flatten()
            .map { it.representation }
            .toSet()
            .shouldBe(
                setOf(
                    "if :: Bool -> a -> a -> a",
                    "pair :: a -> b -> (Pair a b)",
                    "(+) :: (Num a) -> (Num a) -> (Num a)",
                    "(-) :: (Num a) -> (Num a) -> (Num a)",
                    "(*) :: (Num a) -> (Num a) -> (Num a)",
                    "(/) :: (Num a) -> (Num a) -> (Num a)",
                    "(**) :: (Num a) -> (Num a) -> (Num a)"
                )
            )
    }
})
