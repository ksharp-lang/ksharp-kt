package org.ksharp.typesystem.unification

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.solver.solve
import org.ksharp.typesystem.types.*

class ParametricUnification : CompoundUnification<ParametricType>() {

    override val Type.isSameTypeClass: Boolean
        get() = this is ParametricType

    private val Type.parametricTypeName: String?
        get() =
            when (this) {
                is TraitType -> this.name
                is Concrete -> this.name
                is ParametricType -> if (this.type is Alias) this.type.name else null
                else -> null
            }

    private fun unifyType(location: Location, type1: ParametricType, type2: ParametricType): ErrorOrType =
        type1.type.solve().flatMap { t1 ->
            type2.type.solve().flatMap { t2 ->
                if (t1.parametricTypeName == t2.parametricTypeName) {
                    if (t1 is ParametricType) Either.Right(type1.type)
                    else Either.Right(t1)
                } else incompatibleType(location, type1, type2)
            }
        }

    override fun compoundUnify(
        location: Location,
        type1: ParametricType,
        type2: ParametricType,
        checker: UnificationChecker
    ): ErrorOrType =
        when {
            type1.type is TraitType ->
                if (checker.isImplemented(type1.type, type2)) {
                    Either.Right(type1)
                } else incompatibleType(location, type1, type2)

            type1.params.size != type2.params.size -> incompatibleType(location, type1, type2)

            else -> {
                unifyType(location, type1, type2).flatMap {
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
