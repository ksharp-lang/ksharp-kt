package org.ksharp.typesystem.unification

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.FixedTraitType
import org.ksharp.typesystem.types.ImplType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type

class ImplUnification : UnificationAlgo<ImplType> {

    override fun unify(
        location: Location,
        type1: ImplType,
        type2: Type,
        checker: UnificationChecker
    ): ErrorOrType =
        if (type1 == type2) Either.Right(type1)
        else
            when (type2) {
                is FixedTraitType -> {
                    if (type1.trait == type2.trait) Either.Right(type2)
                    else incompatibleType(location, type1, type2)
                }

                is TraitType -> {
                    if (type1.trait == type2) Either.Right(FixedTraitType(type2))
                    else incompatibleType(location, type1, type2)
                }

                else -> type2().flatMap { t2 ->
                    type1.impl.unify(location, t2, checker).map {
                        ImplType(type1.trait, it)
                    }.flatMapLeft {
                        if (checker.isImplemented(type1.trait, t2)) Either.Right(FixedTraitType(type1.trait))
                        else incompatibleType(location, type1, type2)
                    }
                }
            }

}
