package org.ksharp.module.prelude.unification

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.module.prelude.types.NumericType
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.ImplType
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.unification.UnificationAlgo
import org.ksharp.typesystem.unification.UnificationChecker
import org.ksharp.typesystem.unification.innerType
import org.ksharp.typesystem.unification.unify

class NumericUnification : UnificationAlgo<NumericType> {
    override fun unify(location: Location, type1: NumericType, type2: Type, checker: UnificationChecker): ErrorOrType =
        type2().flatMap { rType ->
            when (val innerRType = rType.innerType) {
                is Parameter -> Either.Right(type1)
                is NumericType -> {
                    if (innerRType.type.isInteger == type1.type.isInteger
                        && innerRType.type.size <= type1.type.size
                    )
                        Either.Right(type1)
                    else incompatibleType(location, type1, type2)
                }

                is ImplType -> innerRType.impl.unify(location, type1, checker)
                else -> incompatibleType(location, type1, type2)
            }
        }
}
