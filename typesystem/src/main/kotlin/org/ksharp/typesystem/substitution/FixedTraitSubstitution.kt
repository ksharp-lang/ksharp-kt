package org.ksharp.typesystem.substitution

import org.ksharp.common.Either
import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.FixedTraitType
import org.ksharp.typesystem.types.Type

class FixedTraitSubstitution : SubstitutionAlgo<FixedTraitType> {
    private val FixedTraitType.mappingKey
        get() =
            "(${trait.name} ${trait.param})"

    override fun extract(
        context: SubstitutionContext,
        location: Location,
        type1: FixedTraitType,
        type2: Type
    ): ErrorOrValue<Boolean> =
        type2().flatMap {
            if (context.checker.isImplemented(type1.trait, type2)) {
                context.addMapping(location, type1.mappingKey, type1)
                Either.Right(true)
            } else incompatibleType(location, type1, type2)
        }


    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: FixedTraitType,
        typeContext: Type
    ): ErrorOrType = Either.Right(type)
}
