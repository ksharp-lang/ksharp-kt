package org.ksharp.typesystem.unification

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.Labeled
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.Type


val Type.innerType: Type
    get() =
        when (this) {
            is Labeled -> type
            else -> this
        }

class DefaultUnification : UnificationAlgo<Type> {
    override fun unify(location: Location, typeSystem: TypeSystem, type1: Type, type2: Type): ErrorOrType =
        typeSystem(type1).flatMap { lType ->
            val innerLType = lType.innerType
            typeSystem(type2).flatMap { rType ->
                when (val innerRType = rType.innerType) {
                    is Parameter -> Either.Right(lType)
                    innerLType -> Either.Right(innerRType)
                    else -> incompatibleType(location, type1, type2)
                }
            }
        }
}
