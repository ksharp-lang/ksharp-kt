package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class MatchValueNodeTest : StringSpec({
    "Test Node Interface over MatchListValueNode" {
        MatchListValueNode(
            listOf(
                UnitNode(Location.NoProvided)
            ),
            LiteralValueNode("rest", LiteralValueType.Binding, Location.NoProvided),
            Location.NoProvided,
            MatchListValueNodeLocations(Location.NoProvided)
        ).node.apply {
            cast<MatchListValueNode>().apply {
                head.shouldBe(listOf(UnitNode(Location.NoProvided)))
                tail.shouldBe(LiteralValueNode("rest", LiteralValueType.Binding, Location.NoProvided))
                location.shouldBe(Location.NoProvided)
                locations.shouldBe(
                    MatchListValueNodeLocations(
                        Location.NoProvided,
                    )
                )
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(this, Location.NoProvided, UnitNode(Location.NoProvided)),
                    Node(
                        this,
                        Location.NoProvided,
                        LiteralValueNode("rest", LiteralValueType.Binding, Location.NoProvided)
                    )
                )
            )
        }
    }
    "Test Node Interface over MatchAssignmentNode" {
        MatchAssignNode(
            UnitNode(Location.NoProvided),
            LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
            Location.NoProvided,
        ).node.apply {
            cast<MatchAssignNode>().apply {
                match.shouldBe(
                    UnitNode(Location.NoProvided)
                )
                expression.shouldBe(LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided))
                location.shouldBe(Location.NoProvided)
                locations.shouldBe(NoLocationsDefined)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this, Location.NoProvided,
                        UnitNode(Location.NoProvided)
                    ),
                    Node(
                        this,
                        Location.NoProvided,
                        LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided)
                    )
                )
            )
        }
    }
    "Test Node Interface over MatchExpressionBranchNode" {
        MatchExpressionBranchNode(
            UnitNode(Location.NoProvided),
            LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
            Location.NoProvided
        ).node.apply {
            cast<MatchExpressionBranchNode>().apply {
                match.shouldBe(
                    UnitNode(Location.NoProvided)
                )
                expression.shouldBe(LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided))
                location.shouldBe(Location.NoProvided)
                locations.shouldBe(NoLocationsDefined)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this, Location.NoProvided,
                        UnitNode(Location.NoProvided)
                    ),
                    Node(
                        this,
                        Location.NoProvided,
                        LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided)
                    )
                )
            )
        }
    }
    "Test Node interface over MatchConditionValueNode" {
        MatchConditionValueNode(
            MatchConditionalType.And,
            UnitNode(Location.NoProvided),
            LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
            Location.NoProvided
        ).node.apply {
            cast<MatchConditionValueNode>().apply {
                type.shouldBe(MatchConditionalType.And)
                left.shouldBe(
                    UnitNode(Location.NoProvided)
                )
                right.shouldBe(LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided))
                location.shouldBe(Location.NoProvided)
                locations.shouldBe(NoLocationsDefined)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this, Location.NoProvided,
                        UnitNode(Location.NoProvided)
                    ),
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
