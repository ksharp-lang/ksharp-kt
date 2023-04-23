package org.ksharp.typesystem.unification

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.TypeSystemErrorCode
import org.ksharp.typesystem.types.Annotated
import org.ksharp.typesystem.types.Labeled
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.Type

val Type.innerType: Type
    get() =
        when (this) {
            is Labeled -> type
            is Annotated -> type
            else -> this
        }

class DefaultUnification : UnificationAlgo<Type> {
    override fun unify(location: Location, typeSystem: TypeSystem, type1: Type, type2: Type): ErrorOrType =
        typeSystem(type1).flatMap { lType ->
            val innerLType = lType.innerType
            typeSystem(type2).flatMap { rType ->
                when (rType.innerType) {
                    is Parameter -> Either.Right(lType)
                    innerLType -> Either.Right(rType)
                    else -> TypeSystemErrorCode.IncompatibleTypes.new(
                        location,
                        "type1" to type1.representation,
                        "type2" to type2.representation
                    ).let { Either.Left(it) }
                }
            }
        }
}