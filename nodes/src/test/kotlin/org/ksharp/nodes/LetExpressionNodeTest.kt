package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class LetExpressionNodeTest : StringSpec({
    "Test Node Interface over LetExpressionNode" {
        LetExpressionNode(
            listOf(
                MatchAssignNode(
                    MatchValueNode(
                        MatchValueType.Expression,
                        UnitNode(Location.NoProvided),
                        Location.NoProvided
                    ),
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                    MatchAssignNodeLocations(Location.NoProvided)
                )
            ),
            LiteralValueNode("5", LiteralValueType.Integer, Location.NoProvided),
            Location.NoProvided,
            LetExpressionNodeLocations(Location.NoProvided, Location.NoProvided)
        ).node.apply {
            cast<LetExpressionNode>().apply {
                matches.shouldBe(
                    listOf(
                        MatchAssignNode(
                            MatchValueNode(
                                MatchValueType.Expression,
                                UnitNode(Location.NoProvided),
                                Location.NoProvided,
                            ),
                            LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided,
                            MatchAssignNodeLocations(Location.NoProvided)
                        )
                    )
                )
                expression.shouldBe(LiteralValueNode("5", LiteralValueType.Integer, Location.NoProvided))
                location.shouldBe(Location.NoProvided)
                locations.shouldBe(LetExpressionNodeLocations(Location.NoProvided, Location.NoProvided))
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this, Location.NoProvided, MatchAssignNode(
                            MatchValueNode(
                                MatchValueType.Expression,
                                UnitNode(Location.NoProvided),
                                Location.NoProvided
                            ),
                            LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided,
                            MatchAssignNodeLocations(Location.NoProvided)
                        )
                    ),
                    Node(
                        this,
                        Location.NoProvided,
                        LiteralValueNode("5", LiteralValueType.Integer, Location.NoProvided)
                    )
                )
            )
        }
    }
})
