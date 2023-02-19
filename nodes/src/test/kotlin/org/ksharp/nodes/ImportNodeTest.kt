package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.sequences.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class ImportNodeTest : StringSpec({
    "Test Node Interface over ModuleNode" {
        ImportNode("ksharp.math", "m", Location.NoProvided)
            .node.apply {
                children.shouldBeEmpty()
                cast<ImportNode>().apply {
                    moduleName.shouldBe("ksharp.math")
                    key.shouldBe("m")
                    location.shouldBe(Location.NoProvided)
                }
            }
    }
})