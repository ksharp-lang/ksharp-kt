package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

class AnnotationNodeTest : StringSpec({
    "Test Node interface over AnnotationNode" {
        AnnotationNode(
            "ten",
            mapOf("key 1" to "value 2"),
            Location.NoProvided,
            AnnotationNodeLocations(
                altLocation = Location.NoProvided,
                name = Location.NoProvided,
                attrs = listOf()
            )
        ).node.apply {
            cast<AnnotationNode>().apply {
                name.shouldBe("ten")
                attrs.shouldBe(mapOf("key 1" to "value 2"))
                location.shouldBe(Location.NoProvided)
                locations.shouldBe(
                    AnnotationNodeLocations(
                        altLocation = Location.NoProvided,
                        name = Location.NoProvided,
                        attrs = listOf()
                    )
                )
            }
            parent.shouldBeNull()
            children.toList().shouldBeEmpty()
        }
    }
})
