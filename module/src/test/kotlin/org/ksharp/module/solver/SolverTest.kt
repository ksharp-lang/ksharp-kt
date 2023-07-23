package org.ksharp.module.solver

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.module.prelude.preludeTypeSystem
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.module.prelude.types.NumericType
import org.ksharp.module.prelude.types.charType
import org.ksharp.typesystem.solver.solve

class SolverTest : StringSpec({
    "char reducer" {
        preludeTypeSystem
            .value
            .solve(charType).shouldBe(charType)
    }
    "numeric reducer" {
        val intType = NumericType(Numeric.Int)
        preludeTypeSystem
            .value
            .solve(intType).shouldBe(intType)
    }
})
