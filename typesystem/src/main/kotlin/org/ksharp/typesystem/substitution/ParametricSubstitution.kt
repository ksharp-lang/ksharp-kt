package org.ksharp.typesystem.substitution

import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.ParametricType
import org.ksharp.typesystem.types.TraitType
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
        if (type1.type() == type2.type()) {
            type1.params.extract(context, location, type2.params)
        } else incompatibleType(location, type1, type2)

    private fun substituteParams(
        context: SubstitutionContext,
        location: Location,
        type: ParametricType,
        typeContext: Type
    ) =
        type.params.substitute(context, location, typeContext).map {
            ParametricType(type.typeSystem, type.attributes, type.type, it)
        }

    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: ParametricType,
        typeContext: Type
    ): ErrorOrType =
        when (type.type) {
            is TraitType -> {
                context.getMapping(location, type.representation, type).flatMapLeft {
                    substituteParams(context, location, type, typeContext)
                }
            }

            else -> substituteParams(context, location, type, typeContext)
        }


}
