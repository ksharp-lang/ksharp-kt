package org.ksharp.semantics.inference

import org.ksharp.typesystem.types.Type

interface TypePromise

data class ResolvedTypePromise(
    val type: Type
) : TypePromise

data class MaybePolymorphicTypePromise(
    val name: String,
    val paramName: String,
) : TypePromise