package org.ksharp.compiler.transpiler

import io.kotest.core.spec.style.StringSpec
import org.ksharp.compiler.moduleInfo
import org.ksharp.module.prelude.preludeModule
import org.ksharp.test.shouldBeRight

class TranspilerBaseTest : StringSpec({
    "Extract type from a abstraction" {
        """
            sum a :: (Num a) -> (Num a) -> (Num a)
            sum a b = a + b
        """.trimIndent()
            .moduleInfo("tst.ks", preludeModule)
            .flatMap {
                it.abstractions.first().type
            }.map { it.representation }.shouldBeRight("((Num a) -> (Num a) -> (Num a))")
    }
    "Detect if a abstraction is parametric" {
        """
            sum a :: (Num a) -> (Num a) -> (Num a)
            sum2 :: Int -> Int -> Int
            
            sum a b = a + b
            sum2 a b = a + b
        """.trimIndent()
            .moduleInfo("tst.ks", preludeModule)
            .map {
                it.abstractions.associate { a ->
                    a.name to a.parametric.valueOrNull!!
                }
            }.shouldBeRight(
                mapOf(
                    "sum" to true,
                    "sum2" to false
                )
            )
    }
})