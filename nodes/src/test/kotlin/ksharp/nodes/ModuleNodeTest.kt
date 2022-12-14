package ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class ModuleNodeTest : StringSpec({
    "Test Node Interface over ModuleNode" {
        ModuleNode(
            name = "ksharp.math",
            imports = listOf(ImportNode("ksharp.num", "n"))
        ).node.apply {
            cast<ModuleNode>().apply {
                name.shouldBe("ksharp.math")
                imports.shouldBe(listOf(ImportNode("ksharp.num", "n")))
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(Node(this, ImportNode("ksharp.num", "n")))
            )
        }
    }
})