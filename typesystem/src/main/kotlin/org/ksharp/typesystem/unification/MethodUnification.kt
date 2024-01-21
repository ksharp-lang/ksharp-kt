package org.ksharp.typesystem.unification

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.toFunctionType

class MethodUnification : UnificationAlgo<TraitType.MethodType> {

    private fun toFunctionType(type: Type) =
        if (type is TraitType.MethodType) type.arguments.toFunctionType(type.typeSystem, type.attributes, type.scope)
        else type

    override fun unify(
        location: Location,
        type1: TraitType.MethodType,
        type2: Type,
        checker: UnificationChecker
    ): ErrorOrType =
        if (type1 == type2) Either.Right(type1)
        else toFunctionType(type1).unify(location, toFunctionType(type2), checker)


}
