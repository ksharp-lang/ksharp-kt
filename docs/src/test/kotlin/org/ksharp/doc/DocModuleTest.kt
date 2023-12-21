package org.ksharp.doc

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class DocModuleTest : StringSpec({
    "Search into the MemoryDocModule" {
        docModule(
            listOf(
                DocAbstraction(
                    "sum/2",
                    "",
                    "Int -> Int -> Int",
                    "Sum two numbers"
                ),
                DocAbstraction(
                    "abs/1",
                    "",
                    "Int -> Int",
                    "Return the absolute value of a number"
                ),
                DocAbstraction(
                    "abs/1",
                    "Trait",
                    "Long -> Long",
                    "Return the absolute value of a number"
                )
            )
        ).apply {
            representation("abs/1").shouldBe("Int -> Int")
            representation("sum/2").shouldBe("Int -> Int -> Int")
            representation("abs/1", "Trait").shouldBe("Long -> Long")
        }
    }
})
