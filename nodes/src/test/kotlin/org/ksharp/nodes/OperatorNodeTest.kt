package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class OperatorNodeTest : StringSpec({
    "Test Node Interface over OperatorNode" {
        OperatorNode(
            "+",
            LiteralValueNode("1000.0", LiteralValueType.Decimal, Location.NoProvided),
            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
            Location.NoProvided,
        ).node.apply {
            cast<OperatorNode>().apply {
                operator.shouldBe("+")
                left.shouldBe(LiteralValueNode("1000.0", LiteralValueType.Decimal, Location.NoProvided))
                right.shouldBe(LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided))
                location.shouldBe(Location.NoProvided)
                locations.shouldBe(NoLocationsDefined)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this,
                        Location.NoProvided,
                        LiteralValueNode("1000.0", LiteralValueType.Decimal, Location.NoProvided)
                    ),
                    Node(
                        this,
                        Location.NoProvided,
                        LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided)
                    )
                )
            )
        }
    }
})
