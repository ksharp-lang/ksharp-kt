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
        preludeModule.functions
            .values
            .map {
                println(it.representation)
                it.representation
            }
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
                    "byte :: (Num a) -> Byte",
                    "short :: (Num a) -> Short",
                    "int :: (Num a) -> Int",
                    "long :: (Num a) -> Long",
                    "bigint :: (Num a) -> BigInt",
                    "float :: (Num a) -> Float",
                    "double :: (Num a) -> Double",
                    "bigdec :: (Num a) -> BigDecimal",
                    "(>) :: (Num a) -> (Num a) -> True\n|False",
                    "(>=) :: (Num a) -> (Num a) -> True\n|False",
                    "(<) :: (Num a) -> (Num a) -> True\n|False",
                    "(<=) :: (Num a) -> (Num a) -> True\n|False",
                    "(==) :: a -> a -> True\n|False",
                    "(!=) :: a -> a -> True\n|False",
                )
            )
    }
})
