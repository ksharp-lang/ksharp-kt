package org.ksharp.typesystem.substitution

import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.UnionType

class UnionSubstitution : CompoundSubstitution<UnionType>() {
    override val Type.isSameTypeClass: Boolean
        get() = this is UnionType

    override fun compoundExtract(
        context: SubstitutionContext,
        location: Location,
        type1: UnionType,
        type2: UnionType
    ): ErrorOrValue<Boolean> =
        type1.arguments
            .values.asSequence().extract(context, location, type2.arguments.values.asSequence())

    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: UnionType,
        typeContext: Type
    ): ErrorOrType =
        type.arguments.values
            .substitute(context, location, typeContext)
            .map {
                UnionType(type.typeSystem, type.attributes, it.associate { tp ->
                    tp.cast<UnionType.ClassType>().let { clsType ->
                        clsType.label to clsType
                    }
                })
            }
}

class ClassSubstitution : CompoundSubstitution<UnionType.ClassType>() {
    override val Type.isSameTypeClass: Boolean
        get() = this is UnionType.ClassType

    override fun compoundExtract(
        context: SubstitutionContext,
        location: Location,
        type1: UnionType.ClassType,
        type2: UnionType.ClassType
    ): ErrorOrValue<Boolean> =
        if (type1.label != type2.label) incompatibleType(location, type1, type2)
        else type1.params.extract(context, location, type2.params)

    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: UnionType.ClassType,
        typeContext: Type
    ): ErrorOrType =
        type.params.substitute(context, location, typeContext)
            .map {
                UnionType.ClassType(type.typeSystem, type.label, it)
            }
}
