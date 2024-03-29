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
import org.ksharp.typesystem.unification.UnificationChecker
import org.ksharp.typesystem.unification.unify

class NumericUnificationTest : StringSpec({
    val checker = UnificationChecker { _, _ -> false }
    "numeric and parameter type" {
        val type1 = NumericType(Numeric.Int)
        val type2 = preludeTypeSystem.value.newParameter()
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeRight(type1)
    }
    "Same numeric type" {
        val type1 = NumericType(Numeric.Int)
        val type2 = NumericType(Numeric.Int)
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeRight(type1)
    }
    "Right numeric type same type and less size" {
        val type1 = NumericType(Numeric.Int)
        val type2 = NumericType(Numeric.Short)
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeRight(type1)
    }
    "Right numeric type same type and greater size" {
        val type1 = NumericType(Numeric.Int)
        val type2 = NumericType(Numeric.Long)
        type1.unify(Location.NoProvided, type2, checker)
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
        type1.unify(Location.NoProvided, type2, checker)
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
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to type1.representation,
                    "type2" to type2.representation
                )
            )
    }
})
