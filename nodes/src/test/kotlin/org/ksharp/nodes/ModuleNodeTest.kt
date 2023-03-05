package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
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

class ModuleNodeTest : StringSpec({
    "Test Node Interface over ModuleNode" {
        ModuleNode(
            name = "ksharp.math",
            listOf(ImportNode("ksharp.num", "n", testLocation)),
            listOf(TypeNode(false, "Age", listOf(), ConcreteTypeNode("Int", testLocation), testLocation)),
            listOf(TypeDeclarationNode("sum", listOf(), ConcreteTypeNode("Int", testLocation), testLocation)),
            listOf(),
            testLocation
        ).node.apply {
            cast<ModuleNode>().apply {
                name.shouldBe("ksharp.math")
                imports.shouldBe(listOf(ImportNode("ksharp.num", "n", testLocation)))
                typeDeclarations.shouldBe(
                    listOf(TypeDeclarationNode("sum", listOf(), ConcreteTypeNode("Int", testLocation), testLocation))
                )
                types.shouldBe(
                    listOf(TypeNode(false, "Age", listOf(), ConcreteTypeNode("Int", testLocation), testLocation))
                )
                functions.shouldBeEmpty()
                location.shouldBe(testLocation)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(this, testLocation, ImportNode("ksharp.num", "n", testLocation)),
                    Node(
                        this,
                        testLocation,
                        TypeNode(false, "Age", listOf(), ConcreteTypeNode("Int", testLocation), testLocation)
                    ),
                    Node(
                        this,
                        testLocation,
                        TypeDeclarationNode("sum", listOf(), ConcreteTypeNode("Int", testLocation), testLocation)
                    )
                )
            )
        }
    }
})