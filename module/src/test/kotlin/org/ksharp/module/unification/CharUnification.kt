package org.ksharp.module.unification

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.module.prelude.preludeTypeSystem
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.module.prelude.types.NumericType
import org.ksharp.module.prelude.types.charType
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.TypeSystemErrorCode
import org.ksharp.typesystem.types.newParameter
import org.ksharp.typesystem.unification.unify

class CharUnification : StringSpec({
    "chart type and parameter" {
        val type1 = charType
        val type2 = preludeTypeSystem.value.newParameter()
        preludeTypeSystem.value.unify(Location.NoProvided, type1, type2).shouldBeRight(type1)
    }
    "chart type and other type" {
        val type1 = charType
        val type2 = NumericType(Numeric.Int)
        preludeTypeSystem.value.unify(Location.NoProvided, type1, type2)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to type1.representation,
                    "type2" to type2.representation
                )
            )
    }
})
