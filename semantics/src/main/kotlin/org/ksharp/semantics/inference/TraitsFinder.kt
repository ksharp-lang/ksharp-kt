package org.ksharp.semantics.inference

import org.ksharp.common.cast
import org.ksharp.typesystem.types.Concrete
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type

fun getTraitsImplemented(type: Type, info: InferenceModuleInfo): Sequence<TraitType> =
    info.typeSystem.let { typeSystem ->
        type().map { resolvedType ->
            when (resolvedType) {
                is Concrete -> resolvedType.name.let { typeName ->
                    info.impls
                        .filter { it.type == typeName }
                        .map {
                            typeSystem[it.trait].valueOrNull!!
                        }.cast<Sequence<TraitType>>()
                }

                is Parameter ->
                    info.traits

                else -> emptySequence()
            }
        }.valueOrNull ?: emptySequence()
    }
