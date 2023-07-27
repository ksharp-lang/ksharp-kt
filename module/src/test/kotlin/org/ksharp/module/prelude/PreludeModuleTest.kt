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
                    "if :: True\n|False -> a -> a -> a",
                    "pair :: a -> b -> (Pair a b)",
                    "tupleOf :: a -> a",
                    "listOf :: a -> (List a)",
                    "emptyList :: Unit -> (List a)",
                    "setOf :: a -> (Set a)",
                    "emptySet :: Unit -> (List a)",
                    "mapOf :: (Pair k v) -> (Map k v)",
                    "emptyMap :: Unit -> (Map k v)",
                    "(+) :: (Num a) -> (Num a) -> (Num a)",
                    "(-) :: (Num a) -> (Num a) -> (Num a)",
                    "(*) :: (Num a) -> (Num a) -> (Num a)",
                    "(/) :: (Num a) -> (Num a) -> (Num a)",
                    "(**) :: (Num a) -> (Num a) -> (Num a)",
                    "(%) :: (Num a) -> (Num a) -> (Num a)",
                    "byte :: (Num a) -> (Num numeric<Byte>)",
                    "short :: (Num a) -> (Num numeric<Short>)",
                    "int :: (Num a) -> (Num numeric<Int>)",
                    "long :: (Num a) -> (Num numeric<Long>)",
                    "bigint :: (Num a) -> (Num numeric<BigInt>)",
                    "float :: (Num a) -> (Num numeric<Float>)",
                    "double :: (Num a) -> (Num numeric<Double>)",
                    "bigdec :: (Num a) -> (Num numeric<BigDecimal>)"
                )
            )
    }
})
