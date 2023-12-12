package org.ksharp.typesystem.substitution

import org.ksharp.common.ErrorOrValue
import org.ksharp.common.HandlePromise
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.FullFunctionType
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.PartialFunctionType
import org.ksharp.typesystem.types.Type

abstract class FunctionSubstitution<T : FunctionType> : CompoundSubstitution<T>() {

    abstract fun createSubstituteType(
        originalType: T,
        typeSystem: HandlePromise<TypeSystem>,
        attributes: Set<Attribute>,
        arguments: List<Type>
    ): T

    override fun compoundExtract(
        context: SubstitutionContext,
        location: Location,
        type1: T,
        type2: T
    ): ErrorOrValue<Boolean> =
        type1.arguments.extract(context, location, type2.arguments)

    override fun substitute(
        context: SubstitutionContext,
        location: Location,
        type: T,
        typeContext: Type
    ): ErrorOrType =
        type.arguments
            .substitute(context, location, typeContext)
            .map {
                createSubstituteType(type, type.typeSystem, type.attributes, it)
            }
}

class FullFunctionSubstitution : FunctionSubstitution<FullFunctionType>() {

    override val Type.isSameTypeClass: Boolean
        get() = this is FullFunctionType

    override fun createSubstituteType(
        originalType: FullFunctionType,
        typeSystem: HandlePromise<TypeSystem>,
        attributes: Set<Attribute>,
        arguments: List<Type>
    ): FullFunctionType =
        FullFunctionType(typeSystem, attributes, arguments)

}

class PartialFunctionSubstitution : FunctionSubstitution<PartialFunctionType>() {

    override val Type.isSameTypeClass: Boolean
        get() = this is PartialFunctionType

    override fun createSubstituteType(
        originalType: PartialFunctionType,
        typeSystem: HandlePromise<TypeSystem>,
        attributes: Set<Attribute>,
        arguments: List<Type>
    ): PartialFunctionType =
        PartialFunctionType(arguments, originalType.function)

}
