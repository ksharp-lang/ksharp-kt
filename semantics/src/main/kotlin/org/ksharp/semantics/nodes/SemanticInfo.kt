package org.ksharp.semantics.nodes

import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.nodes.semantic.SemanticInfo
import org.ksharp.nodes.semantic.Symbol
import org.ksharp.nodes.semantic.TypePromise
import org.ksharp.nodes.semantic.TypeSemanticInfo
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.newParameter

fun TypeSystem.getTypeSemanticInfo(name: String) =
    TypeSemanticInfo(get(name))

fun SemanticInfo.getType(location: Location): ErrorOrType =
    when (this) {
        is TypeSemanticInfo -> if (hasInferredType()) getInferredType(location) else type
        is Symbol -> if (hasInferredType()) getInferredType(location) else type.getType(location)
        else -> getInferredType(location)
    }

fun TypePromise.getType(location: Location): ErrorOrType =
    this.cast<SemanticInfo>().getType(location)

fun TypeSystem.paramTypePromise() = TypeSemanticInfo(Either.Right(newParameter()))

fun Error.toTypePromise() = TypeSemanticInfo(Either.Left(this))
