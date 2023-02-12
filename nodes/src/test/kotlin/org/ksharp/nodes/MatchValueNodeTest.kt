package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class MatchValueNodeTest : StringSpec({
    "Test Node Interface over MatchValueNode" {
        MatchValueNode(
            MatchValueType.Expression,
            UnitNode(Location.NoProvided),
            Location.NoProvided
        ).node.apply {
            cast<MatchValueNode>().apply {
                type.shouldBe(MatchValueType.Expression)
                value.shouldBe(UnitNode(Location.NoProvided))
                location.shouldBe(Location.NoProvided)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(this, Location.NoProvided, UnitNode(Location.NoProvided))
                )
            )
        }
    }
    "Test Node Interface over MatchListValueNode" {
        MatchListValueNode(
            listOf(
                UnitNode(Location.NoProvided)
            ),
            LiteralValueNode("rest", LiteralValueType.Binding, Location.NoProvided),
            Location.NoProvided
        ).node.apply {
            cast<MatchListValueNode>().apply {
                head.shouldBe(listOf(UnitNode(Location.NoProvided)))
                tail.shouldBe(LiteralValueNode("rest", LiteralValueType.Binding, Location.NoProvided))
                location.shouldBe(Location.NoProvided)
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
})