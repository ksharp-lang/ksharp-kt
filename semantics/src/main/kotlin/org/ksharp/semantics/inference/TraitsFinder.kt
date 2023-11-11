package org.ksharp.semantics.inference

import org.ksharp.common.cast
import org.ksharp.module.FunctionInfo
import org.ksharp.module.Impl
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.ParametricType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.unification.UnificationChecker

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
    getTraitsImplemented(type, context)
        .any { t ->
            trait == t
        }
}

private fun findTraits(type: Type, context: TraitFinderContext) =
    context.typeSystem.let { typeSystem ->
        context.impls
            .filter { it.type == type }
            .map {
                typeSystem[it.trait].valueOrNull!!
            }.cast<Sequence<TraitType>>()
    }

fun getTraitsImplemented(type: Type, context: TraitFinderContext): Sequence<TraitType> =
    type().map { resolvedType ->
        when {
            resolvedType is Parameter ->
                context.typeSystem.asSequence()
                    .map { it.second }
                    .filterIsInstance<TraitType>()

            resolvedType is ParametricType && resolvedType.params.size == 1 -> {
                val rType = resolvedType.typeSystem
                    .handle!![resolvedType.type.representation]
                    .valueOrNull!!
                if (rType is TraitType) {
                    sequenceOf(rType)
                } else emptySequence<TraitType>()
            }

            resolvedType is TraitType -> sequenceOf(resolvedType)
            else -> findTraits(resolvedType, context)
        }
    }.valueOrNull ?: emptySequence()
