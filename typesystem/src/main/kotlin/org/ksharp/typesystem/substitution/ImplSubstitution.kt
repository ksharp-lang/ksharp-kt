package org.ksharp.typesystem.substitution

import org.ksharp.common.Either
import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.FixedTraitType
import org.ksharp.typesystem.types.ImplType
import org.ksharp.typesystem.types.Type

class ImplSubstitution : SubstitutionAlgo<ImplType> {

    private val ImplType.mappingKey
        get() =
            "(${trait.name} ${trait.param})"

    override fun extract(
        context: SubstitutionContext,
        location: Location,
        type1: ImplType,
        type2: Type
    ): ErrorOrValue<Boolean> =
        type2().flatMap { t2 ->
            when (t2) {
                is FixedTraitType -> if (context.checker.isImplemented(type1.trait, t2.trait)) {
                    context.addMapping(location, type1.mappingKey, t2)
                    Either.Right(true)
                } else incompatibleType(location, type1, type2)

                else -> if (context.checker.isImplemented(type1.trait, t2)) {
                    context.addMapping(location, type1.mappingKey, type1)
                    Either.Right(true)
                } else incompatibleType(location, type1, type2)
            }
        }

    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: ImplType,
        typeContext: Type
    ): ErrorOrType = context.getMapping(location, type.mappingKey, type)
}
