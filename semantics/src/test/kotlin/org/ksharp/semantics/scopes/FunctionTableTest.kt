package org.ksharp.semantics.scopes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.nodes.semantic.TypeSemanticInfo
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.types.newParameterForTesting

class FunctionTableTest : StringSpec({
    val mockType = TypeSemanticInfo(Either.Right(newParameterForTesting(0)))
    "Add function into symbol table" {
        FunctionTableBuilder(ErrorCollector()).apply {
            register(
                "sum",
                Function(setOf(CommonAttribute.Public), "sum", listOf(mockType)),
                Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            this["sum"]!!.apply {
                first.shouldBe(
                    Function(setOf(CommonAttribute.Public), "sum", listOf(mockType))
                )
                second.shouldBe(Location.NoProvided)
                isPublic.shouldBeTrue()
                isInternal.shouldBeFalse()
            }
            this["sub"].shouldBeNull()
        }
    }
    "Already add type" {
        FunctionTableBuilder(ErrorCollector()).apply {
            register(
                "sum",
                Function(setOf(CommonAttribute.Internal), "sum", listOf(mockType)),
                Location.NoProvided
            ).shouldBeRight()
            register(
                "sum",
                Function(setOf(CommonAttribute.Public), "sub", listOf(mockType)),
                Location.NoProvided
            ).shouldBeLeft(
                TableErrorCode.AlreadyDefined.new(
                    Location.NoProvided,
                    "classifier" to "Function",
                    "name" to "sum"
                )
            )
        }.build().apply {
            this["sum"]!!.apply {
                this.first.shouldBe(
                    Function(setOf(CommonAttribute.Internal), "sum", listOf(mockType))
                )
                second.shouldBe(Location.NoProvided)
                isPublic.shouldBeFalse()
                isInternal.shouldBeTrue()
            }
        }
    }
})
