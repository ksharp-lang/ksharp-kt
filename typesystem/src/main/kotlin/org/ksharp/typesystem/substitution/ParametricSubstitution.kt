package org.ksharp.typesystem.substitution

import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.ParametricType
import org.ksharp.typesystem.types.Type

class ParametricSubstitution : CompoundSubstitution<ParametricType>() {

    override val Type.isSameTypeClass: Boolean
        get() = this is ParametricType

    override fun compoundExtract(
        context: SubstitutionContext,
        location: Location,
        type1: ParametricType,
        type2: ParametricType
    ): ErrorOrValue<Boolean> =
        context.extract(location, type1.type, type2.type).flatMap {
            type1.params.extract(context, location, type2.params)
        }

    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: ParametricType,
        typeContext: Type
    ): ErrorOrType =
        context.substitute(location, type.type, typeContext).flatMap { tp ->
            type.params.substitute(context, location, typeContext).map {
                ParametricType(tp, it)
            }
        }

}