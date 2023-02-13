package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldBeEmpty
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
            imports = mapOf("n" to ImportNode("ksharp.num", "n", testLocation)),
            mapOf(),
            mapOf(),
            testLocation
        ).node.apply {
            cast<ModuleNode>().apply {
                name.shouldBe("ksharp.math")
                imports.shouldBe(mapOf("n" to ImportNode("ksharp.num", "n", testLocation)))
                typeDeclarations.shouldBeEmpty()
                types.shouldBeEmpty()
                location.shouldBe(testLocation)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(Node(this, testLocation, ImportNode("ksharp.num", "n", testLocation)))
            )
        }
    }
})