package org.ksharp.typesystem.unification

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.Type

abstract class CompoundUnification<T : Type> : UnificationAlgo<T> {

    abstract val Type.isSameTypeClass: Boolean
    override fun unify(location: Location, typeSystem: TypeSystem, type1: T, type2: Type): ErrorOrType =
        typeSystem(type2).flatMap { rType ->
            val innerRType = rType.innerType
            when {
                innerRType is Parameter -> Either.Right(type1)
                innerRType.isSameTypeClass -> compoundUnify(location, typeSystem, type1, innerRType.cast<T>())
                else -> incompatibleType(location, type1, type2)
            }
        }

    abstract fun compoundUnify(location: Location, typeSystem: TypeSystem, type1: T, type2: T): ErrorOrType

}