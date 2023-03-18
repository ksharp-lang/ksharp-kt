package org.ksharp.semantics.scopes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight

class TypeVisibilityTableTest : StringSpec({
    "Create type visibility table" {
        TypeVisibilityTableBuilder(ErrorCollector()).apply {
            register("Int", TypeVisibility.Public, Location.NoProvided).shouldBeRight(true)
            register("Double", TypeVisibility.Internal, Location.NoProvided).shouldBeRight(true)
            register("Int", TypeVisibility.Internal, Location.NoProvided).shouldBeLeft(
                TableErrorCode.AlreadyDefined.new(Location.NoProvided, "classifier" to "Type", "name" to "Int")
            )
        }.build().apply {
            this["Int"]
                .shouldNotBeNull().apply {
                    isPublic.shouldBeTrue()
                    isInternal.shouldBeFalse()
                }
            this["Double"]
                .shouldNotBeNull().apply {
                    isInternal.shouldBeTrue()
                    isPublic.shouldBeFalse()
                }
        }
    }
})