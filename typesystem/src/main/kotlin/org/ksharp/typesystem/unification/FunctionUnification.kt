package org.ksharp.typesystem.unification

import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.Type

class FunctionUnification : CompoundUnification<FunctionType>() {
    override val Type.isSameTypeClass: Boolean
        get() = this is FunctionType

    override fun compoundUnify(
        location: Location,
        typeSystem: TypeSystem,
        type1: FunctionType,
        type2: FunctionType
    ): ErrorOrType =
        if (type1.arguments.size != type2.arguments.size) incompatibleType(location, type1, type2)
        else {
            unifyListOfTypes(location, typeSystem, type1, type2, type1.arguments, type2.arguments).map { params ->
                FunctionType(type1.visibility, params)
            }
        }
}