package org.ksharp.semantics.inference

import org.ksharp.common.cast
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type

private fun findTraits(type: Type, info: InferenceContext) =
    info.typeSystem.let { typeSystem ->
        info.impls
            .filter { it.type == type }
            .map {
                typeSystem[it.trait].valueOrNull!!
            }.cast<Sequence<TraitType>>()
    }

fun getTraitsImplemented(type: Type, info: InferenceContext): Sequence<TraitType> =
    type().map { resolvedType ->
        when (resolvedType) {
            is Parameter ->
                info.typeSystem.asSequence()
                    .map { it.second }
                    .filterIsInstance<TraitType>()

            else -> findTraits(resolvedType, info)
        }
    }.valueOrNull ?: emptySequence()
