package org.ksharp.typesystem.substitution

import org.ksharp.common.*
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.unification.innerType

abstract class CompoundSubstitution<T : Type> : SubstitutionAlgo<T> {
    abstract val Type.isSameTypeClass: Boolean

    protected fun List<Type>.extract(
        context: SubstitutionContext,
        location: Location,
        type: List<Type>
    ): ErrorOrValue<Boolean> =
        this.asSequence().zip(type.asSequence()) { type1, type2 ->
            context.extract(location, type1, type2)
        }.firstOrNull { it.isLeft }
            ?: Either.Right(true)

    protected fun List<Type>.substitute(
        context: SubstitutionContext,
        location: Location,
        typeContext: Type
    ): ErrorOrValue<List<Type>> {
        val types = listBuilder<Type>()
        var result: ErrorOrValue<List<Type>>? = null
        for (type in this) {
            val substitutionResult = context.substitute(location, type, typeContext)
            if (substitutionResult.isLeft) {
                result = substitutionResult.cast()
                break
            }
            types.add(substitutionResult.cast<Either.Right<Type>>().value)
        }
        return result ?: Either.Right(types.build())
    }

    abstract fun compoundExtract(
        context: SubstitutionContext,
        location: Location,
        type1: T,
        type2: T
    ): ErrorOrValue<Boolean>

    override fun extract(
        context: SubstitutionContext,
        location: Location,
        type1: T,
        type2: Type
    ): ErrorOrValue<Boolean> =
        context.typeSystem(type2).flatMap { cType2 ->
            val innerCType2 = cType2.innerType
            when {
                innerCType2.isSameTypeClass -> compoundExtract(context, location, type1, innerCType2.cast<T>())
                else -> incompatibleType(location, type1, type2)
            }
        }

}