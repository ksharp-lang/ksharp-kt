package org.ksharp.semantics.inference

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.common.isRight
import org.ksharp.module.FunctionInfo
import org.ksharp.module.Impl
import org.ksharp.module.prelude.preludeModule
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.ParametricType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.unification.UnificationChecker
import org.ksharp.typesystem.unification.unify

private class TraitMethodTypeInfo(
    override val name: String,
    methodType: TraitType.MethodType
) : FunctionInfo {
    override val attributes: Set<Attribute> = methodType.attributes
    override val types: List<Type> = methodType.arguments
}

interface TraitFinderContext {
    val typeSystem: TypeSystem
    val impls: Sequence<Impl>

    fun findTraitFunction(methodName: String, type: Type): FunctionInfo? =
        getTraitsImplemented(type, this).mapNotNull { trait ->
            trait.methods[methodName]?.let(::methodTypeToFunctionInfo)
        }.firstOrNull()

    fun methodTypeToFunctionInfo(method: TraitType.MethodType): FunctionInfo =
        TraitMethodTypeInfo(method.name, method)
}

fun unificationChecker(context: TraitFinderContext) = UnificationChecker { trait, type ->
    getTraitsImplemented(type, context).also {
        println(it.toList())
    }
        .any { t ->
            trait == t
        }
}

private fun Sequence<Impl>.filterTraits(
    location: Location,
    type: Type,
    typeSystem: TypeSystem,
    checker: UnificationChecker
): Sequence<TraitType> =
    filter { it.type.unify(location, type, checker).isRight }
        .map {
            typeSystem[it.trait].valueOrNull!!
        }.cast()

private fun findTraits(type: Type, context: TraitFinderContext): Sequence<TraitType> =
    unificationChecker(context).let { checker ->
        sequenceOf(
            context.impls
                .filterTraits(Location.NoProvided, type, context.typeSystem, checker),
            preludeModule.impls.asSequence()
                .filterTraits(Location.NoProvided, type, preludeModule.typeSystem, checker)
        ).flatten()
    }

private val ParametricType.traitOrNull: TraitType?
    get() =
        if (type is TraitType) type.cast()
        else typeSystem
            .handle!![type.representation]
            .valueOrNull as? TraitType

fun getTraitsImplemented(type: Type, context: TraitFinderContext): Sequence<TraitType> =
    type().map { resolvedType ->
        when {
            resolvedType is Parameter ->
                sequenceOf(
                    context.typeSystem.asSequence(),
                    preludeModule.typeSystem.asSequence()
                ).flatten()
                    .map { it.second }
                    .filterIsInstance<TraitType>()

            resolvedType is ParametricType && resolvedType.params.size == 1 -> {
                resolvedType.traitOrNull?.let {
                    sequenceOf(it)
                } ?: findTraits(resolvedType, context)
            }

            resolvedType is TraitType -> sequenceOf(resolvedType)
            else -> findTraits(resolvedType, context)
        }
    }.valueOrNull ?: emptySequence()
