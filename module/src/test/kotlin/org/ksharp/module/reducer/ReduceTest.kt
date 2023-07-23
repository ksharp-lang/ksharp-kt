package org.ksharp.module.reducer

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.module.prelude.preludeTypeSystem
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.module.prelude.types.NumericType
import org.ksharp.module.prelude.types.charType
import org.ksharp.typesystem.reducer.reduce

class ReduceTest : StringSpec({
    "char reducer" {
        preludeTypeSystem
            .value
            .reduce(charType).shouldBe(charType)
    }
    "numeric reducer" {
        val intType = NumericType(Numeric.Int)
        preludeTypeSystem
            .value
            .reduce(intType).shouldBe(intType)
    }
})
