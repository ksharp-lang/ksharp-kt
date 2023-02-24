package org.ksharp.analysis.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.analysis.errors.ErrorCollector
import org.ksharp.common.Either
import org.ksharp.common.new

class ErrorCollectorTest : StringSpec({
    "Error collector test" {
        ErrorCollector().apply {
            collect(Either.Left(TypeVisibilityErrorCode.AlreadyDefined.new("type" to "Int")))
            collect(Either.Right(true))
        }.build().apply {
            shouldBe(listOf(TypeVisibilityErrorCode.AlreadyDefined.new("type" to "Int")))
        }
    }
})