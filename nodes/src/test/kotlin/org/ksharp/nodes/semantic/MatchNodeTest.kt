package org.ksharp.nodes.semantic

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.nodes.MatchConditionalType
import org.ksharp.nodes.Node

class MatchNodeTest : StringSpec({
    "Test Node Interface over ListMatchValueNode" {
        ListMatchValueNode(
            listOf(
                VarNode(
                    "a",
                    "VarInfo",
                    Location.NoProvided
                )
            ),
            VarNode(
                "b",
                "VarInfo",
                Location.NoProvided
            ),
            "ListMatchInfo",
            Location.NoProvided
        ).node.apply {
            cast<ListMatchValueNode<String>>().apply {
                head.shouldBe(
                    listOf(
                        VarNode(
                            "a",
                            "VarInfo",
                            Location.NoProvided
                        )
                    )
                )
                tail.shouldBe(
                    VarNode(
                        "b",
                        "VarInfo",
                        Location.NoProvided
                    )
                )
                info.shouldBe("ListMatchInfo")
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
                    ),
                    Node(
                        this,
                        Location.NoProvided,
                        VarNode(
                            "b",
                            "VarInfo",
                            Location.NoProvided
                        )
                    )
                )
            )
        }
    }
    "Test Node Interface over ConditionalMatchValueNode" {
        ConditionalMatchValueNode(
            MatchConditionalType.Or,
            VarNode(
                "a",
                "VarInfo",
                Location.NoProvided
            ),
            VarNode(
                "b",
                "VarInfo",
                Location.NoProvided
            ),
            "ConditionalMatchInfo",
            Location.NoProvided
        ).node.apply {
            cast<ConditionalMatchValueNode<String>>().apply {
                type.shouldBe(MatchConditionalType.Or)
                left.shouldBe(
                    VarNode(
                        "a",
                        "VarInfo",
                        Location.NoProvided
                    )
                )
                left.shouldBe(
                    VarNode(
                        "b",
                        "VarInfo",
                        Location.NoProvided
                    )
                )
                info.shouldBe("ConditionalMatchInfo")
                location.shouldBe(Location.NoProvided)
            }
        }
    }
    "Test Node Interface over MatchBranchNode" {
        MatchBranchNode(
            listOf(
                VarNode(
                    "a",
                    "VarInfo",
                    Location.NoProvided
                )
            ),
            VarNode(
                "b",
                "VarInfo",
                Location.NoProvided
            ),
            "MatchBranchInfo",
            Location.NoProvided
        ).node.apply {
            cast<MatchBranchNode<String>>().apply {
                matches.shouldBe(
                    listOf(
                        VarNode(
                            "a",
                            "VarInfo",
                            Location.NoProvided
                        )
                    )
                )
                expression.shouldBe(
                    VarNode(
                        "b",
                        "VarInfo",
                        Location.NoProvided
                    )
                )
                info.shouldBe("MatchBranchInfo")
                location.shouldBe(Location.NoProvided)
            }
        }
    }
    "Test Node Interface over MatchNode" {
        MatchNode(
            listOf(
                MatchBranchNode(
                    listOf(
                        VarNode(
                            "a",
                            "VarInfo",
                            Location.NoProvided
                        )
                    ),
                    VarNode(
                        "b",
                        "VarInfo",
                        Location.NoProvided
                    ),
                    "MatchBranchInfo",
                    Location.NoProvided
                )
            ),
            VarNode(
                "c",
                "VarInfo",
                Location.NoProvided
            ),
            "MatchInfo",
            Location.NoProvided
        ).node.apply {
            cast<MatchNode<String>>().apply {
                branches.shouldBe(
                    listOf(
                        MatchBranchNode(
                            listOf(
                                VarNode(
                                    "a",
                                    "VarInfo",
                                    Location.NoProvided
                                )
                            ),
                            VarNode(
                                "b",
                                "VarInfo",
                                Location.NoProvided
                            ),
                            "MatchBranchInfo",
                            Location.NoProvided
                        )
                    )
                )
                expression.shouldBe(
                    VarNode(
                        "c",
                        "VarInfo",
                        Location.NoProvided
                    )
                )
                info.shouldBe("MatchInfo")
                location.shouldBe(Location.NoProvided)
            }
        }
    }
})
