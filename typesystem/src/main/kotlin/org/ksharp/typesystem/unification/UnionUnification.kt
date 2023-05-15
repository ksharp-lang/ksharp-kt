package org.ksharp.typesystem.unification

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.TypeConstructor
import org.ksharp.typesystem.types.UnionType

class UnionUnification : UnificationAlgo<UnionType> {
    override fun unify(
        location: Location,
        typeSystem: TypeSystem,
        type1: UnionType,
        type2: Type
    ): ErrorOrType =
        if (type1 == type2) Either.Right(type1)
        else if (type2 is TypeConstructor) {
            if (type1.arguments.containsKey(type2.name))
                Either.Right(type1)
            else incompatibleType(location, type1, type2)
        } else incompatibleType(location, type1, type2)

}