package org.ksharp.typesystem.unification

import org.ksharp.common.*
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.incompatibleType
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

    fun unifyListOfTypes(
        location: Location,
        typeSystem: TypeSystem,
        type1: T,
        type2: T,
        items1: List<Type>,
        items2: List<Type>
    ): ErrorOrValue<List<Type>> {
        var result: ErrorOrValue<List<Type>>? = null
        val type1Params = items1.iterator()
        val type2Params = items2.iterator()
        val params = listBuilder<Type>()
        while (type1Params.hasNext() && type2Params.hasNext()) {
            val item1 = type1Params.next()
            val item2 = type2Params.next()
            val unifyItem = typeSystem.unify(location, item1, item2)
            if (unifyItem.isLeft) {
                result = incompatibleType<Type>(location, type1, type2).cast<Either.Left<Error>>()
                break
            }
            params.add(unifyItem.cast<Either.Right<Type>>().value)
        }
        return result ?: Either.Right(params.build())
    }
}