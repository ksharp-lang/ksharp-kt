package org.ksharp.typesystem.substitution

import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.incompatibleType
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
        if (context.typeSystem(type1.type) == context.typeSystem(type2.type)) {
            type1.params.extract(context, location, type2.params)
        } else incompatibleType(location, type1, type2)

    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: ParametricType,
        typeContext: Type
    ): ErrorOrType =
        type.params.substitute(context, location, typeContext).map {
            ParametricType(type.attributes, type.type, it)
        }

}
