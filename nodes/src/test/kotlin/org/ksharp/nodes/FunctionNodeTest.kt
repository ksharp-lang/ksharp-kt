package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class FunctionNodeTest : StringSpec({
    "Test Node interface over FunctionNode" {
        FunctionNode(
            false,
            true,
            listOf(),
            "ten",
            listOf("a"),
            LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
            Location.NoProvided
        ).node.apply {
            cast<FunctionNode>().apply {
                native.shouldBeFalse()
                pub.shouldBeTrue()
                annotations.shouldBeEmpty()
                name.shouldBe("ten")
                parameters.shouldBe(listOf("a"))
                expression.shouldBe(LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided))
                location.shouldBe(Location.NoProvided)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this,
                        Location.NoProvided,
                        LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided)
                    )
                )
            )
        }
    }
})
