package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import org.ksharp.common.Location
import org.ksharp.nodes.ConcreteTypeNode
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.TypeNode

class TypeSystemSemanticsTest : StringSpec({
    "TypeSystem semantics" {
        ModuleNode(
            "module",
            listOf(),
            listOf(
                TypeNode(
                    false,
                    "Integer",
                    listOf(),
                    ConcreteTypeNode("Int", Location.NoProvided),
                    Location.NoProvided
                )
            ),
            listOf(),
            listOf(),
            Location.NoProvided
        ).checkSemantics().apply {
            errors.shouldBeEmpty()
            typeSystemTable["Integer"]!!
                .apply {
                    isInternal.shouldBeFalse()
                    isPublic.shouldBeTrue()
                }
        }
    }
})