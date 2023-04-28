package org.ksharp.typesystem.substitution

import org.ksharp.common.Either
import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.Type

class IdentitySubstitution : SubstitutionAlgo<Type> {
    override fun extract(
        context: SubstitutionContext,
        location: Location,
        type1: Type,
        type2: Type
    ): ErrorOrValue<Boolean> =
        Either.Right(false)

    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: Type,
        typeContext: Type
    ): ErrorOrType =
        Either.Right(type)

}