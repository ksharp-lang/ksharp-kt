package org.ksharp.module.prelude

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.module.FunctionInfo

private val FunctionInfo.representation: String
    get() = "$name :: ${
        type.joinToString(" -> ") {
            it.representation
        }
    }"

class PreludeModuleTest : StringSpec({
    "Test prelude module" {
        preludeModule.functions.values
            .map { it.representation }
            .shouldBe(
                listOf(
                    "(+) :: (Num a) -> (Num a) -> (Num a)"
                )
            )
    }
})