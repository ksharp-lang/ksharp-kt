package org.ksharp.semantics.inference

import org.ksharp.common.cast
import org.ksharp.typesystem.types.*

private fun findTraits(typeName: String, info: InferenceModuleInfo) =
    info.typeSystem.let { typeSystem ->
        info.impls
            .filter { it.type == typeName }
            .map {
                typeSystem[it.trait].valueOrNull!!
            }.cast<Sequence<TraitType>>()
    }

fun getTraitsImplemented(type: Type, info: InferenceModuleInfo): Sequence<TraitType> =
    type().map { resolvedType ->
        when (resolvedType) {
            is Concrete -> findTraits(resolvedType.name, info)

            is Parameter ->
                info.traits

            is ParametricType -> findTraits(resolvedType.type.cast<Alias>().name, info)

            else -> emptySequence()
        }
    }.valueOrNull ?: emptySequence()
