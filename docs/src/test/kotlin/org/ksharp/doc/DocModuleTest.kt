package org.ksharp.doc

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.io.bufferView
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private fun DocModule.shouldBeSerializable() {
    val output = ByteArrayOutputStream()
    writeTo(output)
    ByteArrayInputStream(output.toByteArray()).bufferView {
        it.readDocModule()
    }.shouldBe(this)
}

class DocModuleTest : StringSpec({
    "Search into the MemoryDocModule" {
        docModule(
            listOf(
                Trait(
                    "Trait",
                    "A trait",
                    listOf(
                        DocAbstraction(
                            "abs/1",
                            "Long -> Long",
                            "Return the absolute value of a number, using trait"
                        )
                    ),
                    listOf("Int", "Long")
                )
            ),
            listOf(
                DocAbstraction(
                    "sum/2",
                    "Int -> Int -> Int",
                    "Sum two numbers"
                ),
                DocAbstraction(
                    "abs/1",
                    "Int -> Int",
                    "Return the absolute value of a number"
                )
            )
        ).apply {
            representation("abs/3").shouldBeNull()
            representation("abs/1").shouldBe("Int -> Int")
            representation("sum/2").shouldBe("Int -> Int -> Int")
            representation("abs/1", "Trait").shouldBe("Long -> Long")
            documentation("abs/1", "Trait2").shouldBeNull()
            documentation("abs/3", "Trait").shouldBeNull()
            documentation("abs/1", "Trait").shouldBe("Return the absolute value of a number, using trait")
            documentation("abs/1").shouldBe("Return the absolute value of a number")
        }
    }
    "Serialize DocModule" {
        docModule(
            listOf(
                Trait(
                    "Trait",
                    "A trait",
                    listOf(
                        DocAbstraction(
                            "abs/1",
                            "Long -> Long",
                            "Return the absolute value of a number, using trait"
                        )
                    ),
                    listOf("Int", "Long")
                )
            ),
            listOf(
                DocAbstraction(
                    "sum/2",
                    "Int -> Int -> Int",
                    "Sum two numbers"
                ),
                DocAbstraction(
                    "abs/1",
                    "Int -> Int",
                    "Return the absolute value of a number"
                )
            )
        ).shouldBeSerializable()
    }
})
