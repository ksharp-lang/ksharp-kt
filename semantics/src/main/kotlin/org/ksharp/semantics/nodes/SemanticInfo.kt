package org.ksharp.semantics.nodes

import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.nodes.semantic.TypeSemanticInfo
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.newParameter

fun TypeSystem.getTypeSemanticInfo(name: String) =
    TypeSemanticInfo(get(name))

fun TypeSystem.paramTypePromise() = TypeSemanticInfo(Either.Right(newParameter()))

fun Error.toTypePromise() = TypeSemanticInfo(Either.Left(this))
