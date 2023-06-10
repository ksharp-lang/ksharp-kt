package org.ksharp.nodes.semantic

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.nodes.NoLocationsDefined
import org.ksharp.nodes.Node

class ApplicationNodeTest : StringSpec({
    "Test Node Interface over ApplicationNode" {
        ApplicationNode(
            ApplicationName(name = "sum5"),
            listOf(
                VarNode(
                    "a",
                    "VarInfo",
                    Location.NoProvided
                )
            ),
            "AppInfo",
            Location.NoProvided
        ).node.apply {
            cast<ApplicationNode<String>>().apply {
                functionName.shouldBe(ApplicationName(name = "sum5"))
                info.shouldBe("AppInfo")
                arguments.shouldBe(
                    listOf(
                        VarNode(
                            "a",
                            "VarInfo",
                            Location.NoProvided
                        )
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
})
