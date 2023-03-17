package org.ksharp.nodes.semantic

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.nodes.Node

class AbstractionNodeTest : StringSpec({
    "Test Node Interface over AbstractionNode" {
        AbstractionNode(
            "id",
            VarNode(
                "a",
                "VarInfo",
                Location.NoProvided
            ),
            "AbstractionNode",
            Location.NoProvided
        ).node.apply {
            cast<AbstractionNode<String>>().apply {
                name.shouldBe("id")
                info.shouldBe("AbstractionNode")
                expression.shouldBe(
                    VarNode(
                        "a",
                        "VarInfo",
                        Location.NoProvided
                    )
                )
                location.shouldBe(Location.NoProvided)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this,
                        Location.NoProvided,
                        VarNode(
                            "a",
                            "VarInfo",
                            Location.NoProvided
                        )
                    )
                )
            )
        }
    }
})