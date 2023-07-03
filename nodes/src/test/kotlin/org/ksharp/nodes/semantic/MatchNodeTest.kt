package org.ksharp.nodes.semantic

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class MatchNodeTest : StringSpec({
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
