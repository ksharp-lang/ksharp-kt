package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class LiteralNodeTest : StringSpec({
    "Test Node Interface over LiteralValueNode" {
        LiteralValueNode(
            "1000",
            LiteralValueType.Integer,
            Location.NoProvided
        ).node.apply {
            cast<LiteralValueNode>().apply {
                value.shouldBe("1000")
                type.shouldBe(LiteralValueType.Integer)
                location.shouldBe(Location.NoProvided)
            }
            parent.shouldBeNull()
            children.toList().shouldBeEmpty()
        }
    }
    "Test Node Interface over LiteralCollectionNode" {
        LiteralCollectionNode(
            listOf(LiteralValueNode("1000.0", LiteralValueType.Decimal, Location.NoProvided)),
            LiteralCollectionType.List,
            Location.NoProvided,
        ).node.apply {
            cast<LiteralCollectionNode>().apply {
                values.shouldBe(listOf(LiteralValueNode("1000.0", LiteralValueType.Decimal, Location.NoProvided)))
                type.shouldBe(LiteralCollectionType.List)
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
                    )
                )
            )
        }
    }
    "Test Node Interface over LiteralMapEntryNode" {
        LiteralMapEntryNode(
            LiteralValueNode("1000.0", LiteralValueType.Decimal, Location.NoProvided),
            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
            Location.NoProvided,
            LiteralMapEntryNodeLocations(Location.NoProvided)
        ).node.apply {
            cast<LiteralMapEntryNode>().apply {
                key.shouldBe(LiteralValueNode("1000.0", LiteralValueType.Decimal, Location.NoProvided))
                value.shouldBe(LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided))
                location.shouldBe(Location.NoProvided)
                locations.shouldBe(LiteralMapEntryNodeLocations(Location.NoProvided))
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
    "Test Node Interface over UnitNode" {
        UnitNode(
            Location.NoProvided
        ).node.apply {
            cast<UnitNode>().apply {
                location.shouldBe(Location.NoProvided)
            }
            parent.shouldBeNull()
            children.toList().shouldBeEmpty()
        }
    }
})
