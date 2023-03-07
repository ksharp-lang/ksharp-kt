package org.ksharp.semantics.expressions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.TypePromise
import org.ksharp.semantics.scopes.TableErrorCode
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight

private class MockTypePromise : TypePromise

class FunctionTableTest : StringSpec({
    val mockType = MockTypePromise()
    "Add function into symbol table" {
        FunctionTableBuilder(ErrorCollector()).apply {
            register(
                "sum",
                Function(FunctionVisibility.Public, "sum", mockType),
                Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            this["sum"]!!.apply {
                first.shouldBe(
                    Function(FunctionVisibility.Public, "sum", mockType)
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
                Function(FunctionVisibility.Internal, "sum", mockType),
                Location.NoProvided
            ).shouldBeRight()
            register(
                "sum",
                Function(FunctionVisibility.Public, "sub", mockType),
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
                    Function(FunctionVisibility.Internal, "sum", mockType)
                )
                second.shouldBe(Location.NoProvided)
                isPublic.shouldBeFalse()
                isInternal.shouldBeTrue()
            }
        }
    }
})