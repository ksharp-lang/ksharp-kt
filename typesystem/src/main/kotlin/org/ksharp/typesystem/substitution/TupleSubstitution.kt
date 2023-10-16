package org.ksharp.typesystem.substitution

import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.TupleType
import org.ksharp.typesystem.types.Type

class TupleSubstitution : CompoundSubstitution<TupleType>() {
    override val Type.isSameTypeClass: Boolean
        get() = this is TupleType

    override fun compoundExtract(
        context: SubstitutionContext,
        location: Location,
        type1: TupleType,
        type2: TupleType
    ): ErrorOrValue<Boolean> = type1.elements.extract(context, location, type2.elements)

    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: TupleType,
        typeContext: Type
    ): ErrorOrType =
        type.elements
            .substitute(context, location, typeContext)
            .map { TupleType(type.typeSystem, type.attributes, it) }

}
