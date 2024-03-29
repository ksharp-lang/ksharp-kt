package org.ksharp.module.substitution

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.module.prelude.preludeTypeSystem
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.module.prelude.types.NumericType
import org.ksharp.module.prelude.types.charType
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.substitution.SubstitutionContext
import org.ksharp.typesystem.substitution.extract
import org.ksharp.typesystem.substitution.substitute
import org.ksharp.typesystem.types.newParameter
import org.ksharp.typesystem.unification.UnificationChecker

class SubstitutionTest : StringSpec({
    val checker = UnificationChecker { _, _ -> false }
    "Char type substitution" {
        val context = SubstitutionContext(checker)
        context.extract(Location.NoProvided, charType, charType)
            .shouldBeRight(false)
        context.substitute(Location.NoProvided, charType, charType)
            .shouldBeRight(charType)
    }
    "Numeric type substitution" {
        val intType = NumericType(Numeric.Int)
        val context = SubstitutionContext(checker)
        context.extract(Location.NoProvided, intType, intType)
            .shouldBeRight(false)
        context.substitute(Location.NoProvided, intType, intType)
            .shouldBeRight(intType)
    }
    "Parameters and numeric types substitution" {
        val intType = NumericType(Numeric.Int)
        val longType = NumericType(Numeric.Long)
        val parameter = preludeTypeSystem.value.newParameter()
        val context = SubstitutionContext(checker)
        context.extract(Location.NoProvided, parameter, intType)
            .shouldBeRight(true)
        context.extract(Location.NoProvided, parameter, longType)
            .shouldBeRight(true)
        context.extract(Location.NoProvided, parameter, intType)
            .shouldBeRight(true)
        context.substitute(Location.NoProvided, parameter, intType)
            .shouldBeRight(longType)
    }
})
