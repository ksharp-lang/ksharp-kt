package org.ksharp.typesystem.unification

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.ParametricType
import org.ksharp.typesystem.types.Type

class ParametricUnification : CompoundUnification<ParametricType>() {

    override val Type.isSameTypeClass: Boolean
        get() = this is ParametricType

    override fun compoundUnify(
        location: Location,
        typeSystem: TypeSystem,
        type1: ParametricType,
        type2: ParametricType
    ): ErrorOrType =
        if (type1.params.size != type2.params.size) incompatibleType(location, type1, type2)
        else {
            val type = if (type1.type.representation == type2.type.representation) {
                Either.Right(type1.type)
            } else incompatibleType(location, type1, type2)
            type.flatMap {
                unifyListOfTypes(location, typeSystem, type1, type2, type1.params, type2.params).map { params ->
                    ParametricType(
                        it.visibility,
                        it,
                        params
                    )
                }
            }
        }
}