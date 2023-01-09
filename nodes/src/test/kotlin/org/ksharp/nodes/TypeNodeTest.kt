package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset

private val testLocation = Location(
    context = "file.ks",
    position = Line(0) to Offset(0)
)

class TypeNodeTest : StringSpec({
    "Test Node Interface over TraitFunctionNode" {
        TraitFunctionNode(
            "sum",
            InvalidSetTypeNode(testLocation),
            testLocation,
        ).node.apply {
            cast<TraitFunctionNode>().apply {
                name.shouldBe("sum")
                type.shouldBe(InvalidSetTypeNode(testLocation))
                location.shouldBe(testLocation)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(Node(this, testLocation, InvalidSetTypeNode(testLocation)))
            )
        }
    }
    "Test Node Interface over TraitFunctionsNode" {
        TraitFunctionsNode(
            listOf(
                TraitFunctionNode(
                    "sum",
                    InvalidSetTypeNode(testLocation),
                    testLocation,
                )
            )
        ).node.apply {
            cast<TraitFunctionsNode>().apply {
                functions.shouldBe(
                    listOf(
                        TraitFunctionNode(
                            "sum",
                            InvalidSetTypeNode(testLocation),
                            testLocation,
                        )
                    )
                )
                location.shouldBe(Location.NoProvided)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this, testLocation, TraitFunctionNode(
                            "sum",
                            InvalidSetTypeNode(testLocation),
                            testLocation,
                        )
                    )
                )
            )
        }
    }
    "Test Node Interface over TraitNode" {
        TraitNode(
            true,
            "Num",
            listOf("a"),
            TraitFunctionsNode(listOf()),
            testLocation
        ).node.apply {
            cast<TraitNode>().apply {
                internal.shouldBeTrue()
                name.shouldBe("Num")
                params.shouldBe(listOf("a"))
                function.shouldBe(TraitFunctionsNode(listOf()))
                location.shouldBe(testLocation)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this, testLocation, TraitFunctionsNode(listOf())
                    )
                )
            )
        }
    }
    "Test Node Interface over LabelTypeNode" {
        LabelTypeNode(
            "key",
            ParameterTypeNode("k", testLocation),
            testLocation
        ).node.apply {
            cast<LabelTypeNode>().apply {
                name.shouldBe("key")
                expr.shouldBe(ParameterTypeNode("k", testLocation))
                location.shouldBe(testLocation)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this, testLocation, ParameterTypeNode("k", testLocation)
                    )
                )
            )
        }
    }
    "Test Node Interface over ConcreteTypeNode" {
        ConcreteTypeNode(
            "Int",
            testLocation
        ).node.apply {
            cast<ConcreteTypeNode>().apply {
                name.shouldBe("Int")
                location.shouldBe(testLocation)
            }
            parent.shouldBeNull()
            children.toList().shouldBeEmpty()
        }
    }
    "Test Node Interface over ParameterTypeNode" {
        ParameterTypeNode(
            "k",
            testLocation
        ).node.apply {
            cast<ParameterTypeNode>().apply {
                name.shouldBe("k")
                location.shouldBe(testLocation)
            }
            parent.shouldBeNull()
            children.toList().shouldBeEmpty()
        }
    }
})