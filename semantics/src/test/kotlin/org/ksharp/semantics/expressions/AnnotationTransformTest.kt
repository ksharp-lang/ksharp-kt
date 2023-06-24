package org.ksharp.semantics.expressions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.nodes.AnnotationNode
import org.ksharp.nodes.AnnotationNodeLocations
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NameAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.attributes.TargetLanguageAttribute

class AnnotationTransformTest : StringSpec({
    "Name attribute calculation" {
        listOf(
            AnnotationNode(
                "name",
                mapOf("default" to "custom-name", "for" to listOf("java", "c#")),
                Location.NoProvided,
                AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            ),
            AnnotationNode(
                "name",
                mapOf("for" to listOf("java", "c#")),
                Location.NoProvided,
                AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            ),
            AnnotationNode(
                "name",
                mapOf("default" to "custom-name-2", "for" to "clojure"),
                Location.NoProvided,
                AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            ),
            AnnotationNode(
                "name",
                mapOf("default" to "custom-name-3", "for" to true),
                Location.NoProvided,
                AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            ),
            AnnotationNode(
                "other-annotation",
                mapOf("default" to "custom-name", "for" to listOf("java", "c#")),
                Location.NoProvided,
                AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            )
        ).toAttributes(NoAttributes)
            .first()
            .cast<NameAttribute>()
            .value
            .shouldBe(mapOf("java" to "custom-name", "c#" to "custom-name", "clojure" to "custom-name-2"))
    }
    "Target language attribute" {
        listOf(
            AnnotationNode(
                "if",
                mapOf("default" to listOf("java", "c#")),
                Location.NoProvided,
                AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            ),
            AnnotationNode(
                "if",
                mapOf("default" to "clojure"),
                Location.NoProvided,
                AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            ),
            AnnotationNode(
                "if",
                mapOf("for" to "clojure"),
                Location.NoProvided,
                AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            ),
            AnnotationNode(
                "if",
                mapOf("default" to true),
                Location.NoProvided,
                AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            ),
            AnnotationNode(
                "other-annotation",
                mapOf("default" to "custom-name", "for" to listOf("java", "c#")),
                Location.NoProvided,
                AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            )
        ).toAttributes(NoAttributes)
            .first()
            .cast<TargetLanguageAttribute>()
            .value
            .shouldBe(setOf("java", "c#", "clojure"))
    }
    "Common attribute" {
    }
    listOf(
        AnnotationNode(
            "sideEffect",
            mapOf(),
            Location.NoProvided,
            AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
        ),
        AnnotationNode(
            "impure",
            mapOf(),
            Location.NoProvided,
            AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
        ),
        AnnotationNode(
            "pure",
            mapOf(),
            Location.NoProvided,
            AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
        ),
        AnnotationNode(
            "constant",
            mapOf(),
            Location.NoProvided,
            AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
        )
    ).toAttributes(NoAttributes)
        .shouldBe(
            setOf(
                CommonAttribute.Impure,
                CommonAttribute.Pure,
                CommonAttribute.Constant
            )
        )
})
