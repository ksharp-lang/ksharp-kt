package org.ksharp.typesystem.substitution

import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.IntersectionType
import org.ksharp.typesystem.types.Type

class IntersectionSubstitution : CompoundSubstitution<IntersectionType>() {

    override val Type.isSameTypeClass: Boolean
        get() = this is IntersectionType

    override fun compoundExtract(
        context: SubstitutionContext,
        location: Location,
        type1: IntersectionType,
        type2: IntersectionType
    ): ErrorOrValue<Boolean> =
        type1.params.extract(context, location, type2.params)

    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: IntersectionType,
        typeContext: Type
    ): ErrorOrType =
        type.params
            .substitute(context, location, typeContext)
            .map { IntersectionType(type.typeSystem, type.attributes, it) }
}
