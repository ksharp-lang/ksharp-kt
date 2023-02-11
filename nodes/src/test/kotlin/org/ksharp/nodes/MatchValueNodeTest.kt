package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class MatchValueNodeTest : StringSpec({
    "Test Node Interface over MatchValueNode" {
        MatchValueNode(
            MatchValueType.Literal,
            UnitNode(Location.NoProvided),
            Location.NoProvided
        ).node.apply {
            cast<MatchValueNode>().apply {
                type.shouldBe(MatchValueType.Literal)
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
})