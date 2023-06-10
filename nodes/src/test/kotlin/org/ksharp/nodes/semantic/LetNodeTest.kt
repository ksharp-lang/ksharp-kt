package org.ksharp.nodes.semantic

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.nodes.NoLocationsDefined
import org.ksharp.nodes.Node

class LetNodeTest : StringSpec({
    "Test Node Interface over LetBindingNode" {
        LetBindingNode(
            VarNode(
                "b",
                "VarInfo",
                Location.NoProvided
            ),
            VarNode(
                "a",
                "VarInfo",
                Location.NoProvided
            ),
            "BindingInfo",
            Location.NoProvided
        ).node.apply {
            cast<LetBindingNode<String>>().apply {
                match.shouldBe(
                    VarNode(
                        "b",
                        "VarInfo",
                        Location.NoProvided
                    )
                )
                info.shouldBe("BindingInfo")
                expression.shouldBe(
                    VarNode(
                        "a",
                        "VarInfo",
                        Location.NoProvided
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
    "Test Node Interface over LetNode" {
        LetNode(
            listOf(
                LetBindingNode(
                    VarNode(
                        "b",
                        "VarInfo",
                        Location.NoProvided
                    ),
                    VarNode(
                        "a",
                        "VarInfo",
                        Location.NoProvided
                    ),
                    "BindingInfo",
                    Location.NoProvided
                )
            ),
            VarNode(
                "a",
                "VarInfo2",
                Location.NoProvided
            ),
            "LetInfo",
            Location.NoProvided
        ).node.apply {
            cast<LetNode<String>>().apply {
                info.shouldBe("LetInfo")
                bindings.shouldBe(
                    listOf(
                        LetBindingNode(
                            VarNode(
                                "b",
                                "VarInfo",
                                Location.NoProvided
                            ),
                            VarNode(
                                "a",
                                "VarInfo",
                                Location.NoProvided
                            ),
                            "BindingInfo",
                            Location.NoProvided
                        )
                    )
                )
                expression.shouldBe(
                    VarNode(
                        "a",
                        "VarInfo2",
                        Location.NoProvided
                    ),
                )
                location.shouldBe(Location.NoProvided)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this,
                        Location.NoProvided,
                        LetBindingNode(
                            VarNode(
                                "b",
                                "VarInfo",
                                Location.NoProvided
                            ),
                            VarNode(
                                "a",
                                "VarInfo",
                                Location.NoProvided
                            ),
                            "BindingInfo",
                            Location.NoProvided
                        )
                    ),
                    Node(
                        this,
                        Location.NoProvided,
                        VarNode(
                            "a",
                            "VarInfo2",
                            Location.NoProvided
                        )
                    )
                )
            )
        }
    }
})
