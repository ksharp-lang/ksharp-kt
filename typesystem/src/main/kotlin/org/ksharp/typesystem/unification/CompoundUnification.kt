package org.ksharp.typesystem.unification

import org.ksharp.common.*
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.Type

abstract class CompoundUnification<T : Type> : UnificationAlgo<T> {

    abstract val Type.isSameTypeClass: Boolean
    override fun unify(location: Location, type1: T, type2: Type, checker: UnificationChecker): ErrorOrType =
        type2().flatMap { rType ->
            val innerRType = rType.innerType
            when {
                innerRType is Parameter -> Either.Right(type1)
                innerRType.isSameTypeClass -> compoundUnify(location, type1, innerRType.cast(), checker)
                else -> elseUnify(location, type1, innerRType, checker)
            }
        }


    open fun elseUnify(location: Location, type1: T, type2: Type, checker: UnificationChecker): ErrorOrType =
        incompatibleType(location, type1, type2)

    abstract fun compoundUnify(location: Location, type1: T, type2: T, checker: UnificationChecker): ErrorOrType

    fun unifyListOfTypes(
        location: Location,
        type1: T,
        type2: T,
        items1: List<Type>,
        items2: List<Type>,
        checker: UnificationChecker
    ): ErrorOrValue<List<Type>> {
        var result: ErrorOrValue<List<Type>>? = null
        val type1Params = items1.iterator()
        val type2Params = items2.iterator()
        val params = listBuilder<Type>()
        while (type1Params.hasNext() && type2Params.hasNext()) {
            val item1 = type1Params.next()
            val item2 = type2Params.next()
            val unifyItem = item1.unify(location, item2, checker)
            if (unifyItem.isLeft) {
                result = incompatibleType<Type>(location, type1, type2).cast<Either.Left<Error>>()
                break
            }
            params.add(unifyItem.cast<Either.Right<Type>>().value)
        }
        return result ?: Either.Right(params.build())
    }
}
