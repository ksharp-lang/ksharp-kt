package org.ksharp.typesystem.unification

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type

class TraitUnification : UnificationAlgo<TraitType> {
    override fun unify(location: Location, type1: TraitType, type2: Type, checker: UnificationChecker): ErrorOrType =
        if (type1 == type2) Either.Right(type1)
        else incompatibleType(location, type1, type2)

}
