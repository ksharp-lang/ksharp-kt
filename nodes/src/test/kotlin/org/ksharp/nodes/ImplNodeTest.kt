package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.sequences.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class ImplNodeTest : StringSpec({
    "Test Node Interface over ImplNode" {
        ImplNode(
            "traitName",
            "forName",
            emptyList(),
            Location.NoProvided,
            ImplNodeLocations(
                Location.NoProvided,
                Location.NoProvided,
                Location.NoProvided,
                Location.NoProvided,
            )
        )
            .node.apply {
                children.shouldBeEmpty()
                cast<ImplNode>().apply {
                    traitName.shouldBe("traitName")
                    forName.shouldBe("forName")
                    functions.shouldBe(emptyList())
                    location.shouldBe(Location.NoProvided)
                    locations.shouldBe(
                        ImplNodeLocations(
                            Location.NoProvided,
                            Location.NoProvided,
                            Location.NoProvided,
                            Location.NoProvided,
                        )
                    )
                }
            }
    }
})
