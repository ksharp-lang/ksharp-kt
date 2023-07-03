package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class MatchExpressionNodeTest : StringSpec({
    "Test Node Interface over MatchExpressionNode" {
        MatchExpressionNode(
            LiteralValueNode("5", LiteralValueType.Integer, Location.NoProvided),
            listOf(
                MatchExpressionBranchNode(
                    UnitNode(Location.NoProvided),
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            ),
            Location.NoProvided,
            MatchExpressionNodeLocations(Location.NoProvided, Location.NoProvided)
        ).node.apply {
            cast<MatchExpressionNode>().apply {
                branches.shouldBe(
                    listOf(
                        MatchExpressionBranchNode(
                            UnitNode(Location.NoProvided),
                            LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        )
                    )
                )
                expression.shouldBe(LiteralValueNode("5", LiteralValueType.Integer, Location.NoProvided))
                location.shouldBe(Location.NoProvided)
                locations.shouldBe(MatchExpressionNodeLocations(Location.NoProvided, Location.NoProvided))
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this,
                        Location.NoProvided,
                        LiteralValueNode("5", LiteralValueType.Integer, Location.NoProvided)
                    ),
                    Node(
                        this, Location.NoProvided, MatchExpressionBranchNode(
                            UnitNode(Location.NoProvided),
                            LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        )
                    )
                )
            )
        }
    }
})
