package org.ksharp.typesystem.substitution

import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.Type

class ParameterSubstitution : SubstitutionAlgo<Parameter> {
    override fun extract(
        context: SubstitutionContext,
        location: Location,
        type1: Parameter,
        type2: Type
    ): ErrorOrValue<Boolean> =
        context.addMapping(location, type1.name, type2).map {
            true
        }

    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: Parameter,
        typeContext: Type
    ): ErrorOrType =
        context.getMapping(location, type.name, typeContext)
}