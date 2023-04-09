package org.ksharp.nodes.semantic

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class ConstantNodeTest : StringSpec({
    "Test Node Interface over ConstantNode" {
        ConstantNode(
            "10",
            "Info",
            Location.NoProvided
        ).node.apply {
            cast<ConstantNode<String>>().apply {
                value.shouldBe("10")
                info.shouldBe("Info")
                location.shouldBe(Location.NoProvided)
            }
            parent.shouldBeNull()
            children.toList().shouldBeEmpty()
        }
    }
})