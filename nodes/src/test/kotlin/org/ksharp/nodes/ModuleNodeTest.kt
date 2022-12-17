package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset

private val location = Location(
    context = "file.ks",
    position = Line(0) to Offset(0)
)

class ModuleNodeTest : StringSpec({
    "Test Node Interface over ModuleNode" {
        ModuleNode(
            name = "ksharp.math",
            imports = listOf(ImportNode("ksharp.num", "n", location)),
            location
        ).node.apply {
            cast<ModuleNode>().apply {
                name.shouldBe("ksharp.math")
                imports.shouldBe(listOf(ImportNode("ksharp.num", "n", location)))
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(Node(this, location, ImportNode("ksharp.num", "n", location)))
            )
        }
    }
})