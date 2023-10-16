package org.ksharp.typesystem.substitution

import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.Type

class FunctionSubstitution : CompoundSubstitution<FunctionType>() {

    override val Type.isSameTypeClass: Boolean
        get() = this is FunctionType

    override fun compoundExtract(
        context: SubstitutionContext,
        location: Location,
        type1: FunctionType,
        type2: FunctionType
    ): ErrorOrValue<Boolean> =
        type1.arguments.extract(context, location, type2.arguments)

    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: FunctionType,
        typeContext: Type
    ): ErrorOrType =
        type.arguments
            .substitute(context, location, typeContext)
            .map { FunctionType(type.typeSystem, type.attributes, it) }
}
