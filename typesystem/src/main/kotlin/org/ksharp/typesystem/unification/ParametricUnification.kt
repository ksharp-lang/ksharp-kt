package org.ksharp.typesystem.unification

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.ImplType
import org.ksharp.typesystem.types.ParametricType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type

class ParametricUnification : CompoundUnification<ParametricType>() {

    override val Type.isSameTypeClass: Boolean
        get() = this is ParametricType

    override fun compoundUnify(
        location: Location,
        type1: ParametricType,
        type2: ParametricType,
        checker: UnificationChecker
    ): ErrorOrType =
        when {
            type1.type is TraitType ->
                if (checker.isImplemented(type1.type, type2)) {
                    Either.Right(ImplType(type1.type.cast(), type2))
                } else incompatibleType(location, type1, type2)

            type1.params.size != type2.params.size -> incompatibleType(location, type1, type2)

            else -> {
                val type = if (type1.type.representation == type2.type.representation) {
                    Either.Right(type1.type)
                } else incompatibleType(location, type1, type2)
                type.flatMap {
                    unifyListOfTypes(location, type1, type2, type1.params, type2.params, checker).map { params ->
                        ParametricType(
                            it.typeSystem,
                            it.attributes,
                            it,
                            params
                        )
                    }
                }
            }
        }
}
