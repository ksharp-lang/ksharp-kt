package ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.sequences.shouldBeEmpty
import io.kotest.matchers.shouldBe

class ImportNodeTest : StringSpec({
    "Test Node Interface over ModuleNode" {
        ImportNode("ksharp.math", "m")
            .node.apply {
                children.shouldBeEmpty()
                cast<ImportNode>().apply {
                    moduleName.shouldBe("ksharp.math")
                    key.shouldBe("m")
                }
            }
    }
})