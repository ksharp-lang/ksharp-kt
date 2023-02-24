package org.ksharp.analysis.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight

class TypeVisibilityTableTest : StringSpec({
    "Create type visibility table" {
        TypeVisibilityTableBuilder().apply {
            register("Int", TypeVisibility.Public).shouldBeRight(true)
            register("Double", TypeVisibility.Internal).shouldBeRight(true)
            register("Int", TypeVisibility.Internal).shouldBeLeft(
                TypeVisibilityErrorCode.AlreadyDefined.new(Location.NoProvided, "type" to "Int")
            )
        }.build().apply {
            this["Int"]
                .shouldNotBeNull()
                .isPublic
                .shouldBeTrue()
            this["Double"]
                .shouldNotBeNull()
                .isInternal
                .shouldBeTrue()
        }
    }
})