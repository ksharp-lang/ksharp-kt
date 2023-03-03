package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Either
import org.ksharp.common.new
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.scopes.TableErrorCode

class ErrorCollectorTest : StringSpec({
    "Error collector test" {
        ErrorCollector().apply {
            collect(Either.Left(TableErrorCode.AlreadyDefined.new("name" to "Int", "classifier" to "Type")))
            collect(Either.Right(true))
        }.build().apply {
            shouldBe(listOf(TableErrorCode.AlreadyDefined.new("name" to "Int", "classifier" to "Type")))
        }
    }
})