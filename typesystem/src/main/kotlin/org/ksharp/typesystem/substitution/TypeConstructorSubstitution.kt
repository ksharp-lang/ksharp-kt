package org.ksharp.typesystem.substitution

import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.TypeConstructor

class TypeConstructorSubstitution : SubstitutionAlgo<TypeConstructor> {
    override fun extract(
        context: SubstitutionContext,
        location: Location,
        type1: TypeConstructor,
        type2: Type
    ): ErrorOrValue<Boolean> =
        context.typeSystem(type1).flatMap {
            context.extract(location, it, type2)
        }

    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: TypeConstructor,
        typeContext: Type
    ): ErrorOrType =
        context.typeSystem(type).flatMap {
            context.substitute(location, it, typeContext)
        }
}