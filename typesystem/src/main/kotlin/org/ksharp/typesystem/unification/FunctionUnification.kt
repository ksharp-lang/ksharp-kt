package org.ksharp.typesystem.unification

import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.FullFunctionType
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.Type

class FunctionUnification : CompoundUnification<FunctionType>() {
    override val Type.isSameTypeClass: Boolean
        get() = this is FunctionType

    override fun compoundUnify(
        location: Location,
        type1: FunctionType,
        type2: FunctionType,
        checker: UnificationChecker
    ): ErrorOrType =
        if (type1.arguments.size != type2.arguments.size) incompatibleType(location, type1, type2)
        else {
            unifyListOfTypes(location, type1, type2, type1.arguments, type2.arguments, checker).map { params ->
                FullFunctionType(type1.typeSystem, type1.attributes, params)
            }
        }
}
