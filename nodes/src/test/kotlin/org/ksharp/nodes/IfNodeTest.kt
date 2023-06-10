package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class IfNodeTest : StringSpec({
    "Test Node Interface over IfNode" {
        IfNode(
            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
            LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided),
            Location.NoProvided,
            IfNodeLocations(Location.NoProvided, Location.NoProvided, Location.NoProvided)
        ).node.apply {
            cast<IfNode>().apply {
                condition.shouldBe(LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided))
                trueExpression.shouldBe(LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided))
                falseExpression.shouldBe(LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided))
                location.shouldBe(Location.NoProvided)
                locations.shouldBe(IfNodeLocations(Location.NoProvided, Location.NoProvided, Location.NoProvided))
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this,
                        Location.NoProvided,
                        LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided)
                    ),
                    Node(
                        this,
                        Location.NoProvided,
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided)
                    ),
                    Node(
                        this,
                        Location.NoProvided,
                        LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided)
                    )
                )
            )
        }
    }
})
