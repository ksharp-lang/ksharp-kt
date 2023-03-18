package org.ksharp.semantics.nodes

import org.ksharp.semantics.inference.TypePromise
import org.ksharp.semantics.inference.getTypePromise
import org.ksharp.semantics.scopes.SymbolTable
import org.ksharp.typesystem.TypeSystem

sealed class SemanticInfo
object EmptySemanticInfo : SemanticInfo()
data class FunctionSemanticInfo(
    val table: SymbolTable,
) : SemanticInfo()

data class TypeSemanticInfo(
    val type: TypePromise
) : SemanticInfo()

fun TypeSystem.getTypeSemanticInfo(name: String) =
    TypeSemanticInfo(getTypePromise(name))