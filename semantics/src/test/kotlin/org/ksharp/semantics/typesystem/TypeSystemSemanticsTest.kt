package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.nodes.ConcreteTypeNode
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.TypeNode
import org.ksharp.semantics.scopes.TableErrorCode
import org.ksharp.test.shouldBeRight

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
            typeSystem["Int"].map { it.representation }.shouldBeRight("(Num numeric<Int>)")
            typeSystem["Integer"].map { it.representation }.shouldBeRight("Int")
        }
    }
    "check Type already defined" {
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
                ),
                TypeNode(
                    true,
                    "Integer",
                    listOf(),
                    ConcreteTypeNode("Long", Location.NoProvided),
                    Location.NoProvided
                )
            ),
            listOf(),
            listOf(),
            Location.NoProvided
        ).checkSemantics().apply {
            errors.shouldBe(
                listOf(
                    TableErrorCode.AlreadyDefined.new(Location.NoProvided, "classifier" to "Type", "name" to "Integer")
                )
            )
            typeSystemTable["Integer"]!!
                .apply {
                    isInternal.shouldBeFalse()
                    isPublic.shouldBeTrue()
                }
            typeSystem["Integer"].map { it.representation }.shouldBeRight("Int")
        }
    }
})