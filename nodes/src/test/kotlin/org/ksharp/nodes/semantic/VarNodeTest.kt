package org.ksharp.nodes.semantic

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.nodes.NoLocationsDefined

class VarNodeTest : StringSpec({
    "Test Node Interface over VarNode" {
        VarNode(
            "a",
            "Info",
            Location.NoProvided
        ).node.apply {
            cast<VarNode<String>>().apply {
                name.shouldBe("a")
                info.shouldBe("Info")
                location.shouldBe(Location.NoProvided)
                locations.shouldBe(NoLocationsDefined)
            }
            parent.shouldBeNull()
            children.toList().shouldBeEmpty()
        }
    }
})
