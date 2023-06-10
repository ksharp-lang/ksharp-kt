package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class FunctionCallNodeTest : StringSpec({
    "Test Node Interface over FunctionCallNode" {
        FunctionCallNode(
            "sum",
            FunctionType.Function,
            listOf(
                LiteralValueNode("1000.0", LiteralValueType.Decimal, Location.NoProvided),
                LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided)
            ),
            Location.NoProvided
        ).node.apply {
            cast<FunctionCallNode>().apply {
                type.shouldBe(FunctionType.Function)
                name.shouldBe("sum")
                arguments.shouldBe(
                    listOf(
                        LiteralValueNode("1000.0", LiteralValueType.Decimal, Location.NoProvided),
                        LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided)
                    )
                )
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
