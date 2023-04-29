package org.ksharp.typesystem.substitution

import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.Annotated
import org.ksharp.typesystem.types.Type

class AnnotatedSubstitution : SubstitutionAlgo<Annotated> {

    override fun extract(
        context: SubstitutionContext,
        location: Location,
        type1: Annotated,
        type2: Type
    ): ErrorOrValue<Boolean> =
        context.extract(location, type1.type, type2)

    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: Annotated,
        typeContext: Type
    ): ErrorOrType =
        context.substitute(location, type.type, typeContext)
}