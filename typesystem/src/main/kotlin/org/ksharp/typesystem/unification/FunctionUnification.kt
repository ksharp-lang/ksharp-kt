package org.ksharp.typesystem.unification

import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.FullFunctionType
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.PartialFunctionType
import org.ksharp.typesystem.types.Type

abstract class FunctionUnification<T : FunctionType> : CompoundUnification<T>() {
    abstract fun createUnificationType(
        originalType: T,
        arguments: List<Type>
    ): T

    override fun compoundUnify(
        location: Location,
        type1: T,
        type2: T,
        checker: UnificationChecker
    ): ErrorOrType =
        if (type1.arguments.size != type2.arguments.size) incompatibleType(location, type1, type2)
        else {
            unifyListOfTypes(location, type1, type2, type1.arguments, type2.arguments, checker).map { params ->
                FullFunctionType(type1.typeSystem, type1.attributes, params, type1.scope)
            }
        }
}

class FullFunctionUnification : FunctionUnification<FullFunctionType>() {

    override val Type.isSameTypeClass: Boolean
        get() = this is FullFunctionType

    override fun createUnificationType(
        originalType: FullFunctionType,
        arguments: List<Type>
    ): FullFunctionType =
        FullFunctionType(originalType.typeSystem, originalType.attributes, arguments, originalType.scope)
}

class PartialFunctionUnification : FunctionUnification<PartialFunctionType>() {

    override val Type.isSameTypeClass: Boolean
        get() = this is PartialFunctionType

    override fun createUnificationType(
        originalType: PartialFunctionType,
        arguments: List<Type>
    ): PartialFunctionType =
        PartialFunctionType(arguments, originalType.function)
}
