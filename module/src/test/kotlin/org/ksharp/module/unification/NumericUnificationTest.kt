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

class NumericUnificationTest : StringSpec({
    "numeric and parameter type" {
        val type1 = NumericType(Numeric.Int)
        val type2 = newParameter()
        preludeTypeSystem.value.unify(Location.NoProvided, type1, type2)
            .shouldBeRight(type1)
    }
    "Same numeric type" {
        val type1 = NumericType(Numeric.Int)
        val type2 = NumericType(Numeric.Int)
        preludeTypeSystem.value.unify(Location.NoProvided, type1, type2)
            .shouldBeRight(type1)
    }
    "Right numeric type same type and less size" {
        val type1 = NumericType(Numeric.Int)
        val type2 = NumericType(Numeric.Short)
        preludeTypeSystem.value.unify(Location.NoProvided, type1, type2)
            .shouldBeRight(type1)
    }
    "Right numeric type same type and greater size" {
        val type1 = NumericType(Numeric.Int)
        val type2 = NumericType(Numeric.Long)
        preludeTypeSystem.value.unify(Location.NoProvided, type1, type2)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to type1.representation,
                    "type2" to type2.representation
                )
            )
    }
    "Right numeric type different type" {
        val type1 = NumericType(Numeric.Int)
        val type2 = NumericType(Numeric.Float)
        preludeTypeSystem.value.unify(Location.NoProvided, type1, type2)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to type1.representation,
                    "type2" to type2.representation
                )
            )
    }
    "Numeric and not numeric type" {
        val type1 = NumericType(Numeric.Int)
        val type2 = charType
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