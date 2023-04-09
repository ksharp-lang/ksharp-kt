package org.ksharp.semantics.inference

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.common.cast
import org.ksharp.module.prelude.preludeModule

class TypePromiseTest : StringSpec({
    "Check TypePromise instantiation" {
        preludeModule.typeSystem.apply {
            getTypePromise("Int").shouldBe(ResolvedTypePromise(get("Int").valueOrNull!!))
            getTypePromise("NotAType").shouldBe(ErrorTypePromise(get("NotAType").cast<Either.Left<Error>>().value))
        }
    }
})