package org.ksharp.module.solver

import io.kotest.core.spec.style.StringSpec
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.module.prelude.types.NumericType
import org.ksharp.module.prelude.types.charType
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.solver.solve

class SolverTest : StringSpec({
    "char reducer" {
        charType.solve().shouldBeRight(charType)
    }
    "numeric reducer" {
        val intType = NumericType(Numeric.Int)
        intType
            .solve().shouldBeRight(intType)
    }
})
