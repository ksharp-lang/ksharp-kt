package org.ksharp.semantics.nodes

import org.ksharp.common.Flag
import org.ksharp.semantics.inference.TypePromise
import org.ksharp.semantics.inference.getTypePromise
import org.ksharp.semantics.inference.type
import org.ksharp.semantics.scopes.SymbolTable
import org.ksharp.semantics.scopes.SymbolTableBuilder
import org.ksharp.semantics.scopes.Table
import org.ksharp.semantics.scopes.TableValue
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem


sealed class SemanticInfo {
    internal val inferFlag: Flag by lazy { Flag() }
}

object EmptySemanticInfo : SemanticInfo()

interface SymbolResolver {
    fun getSymbol(name: String): Symbol?
}

data class SymbolTableSemanticInfo(
    private val table: SymbolTable,
) : SemanticInfo(), SymbolResolver, Table<Symbol> {
    override fun getSymbol(name: String): Symbol? =
        table[name]?.first?.also { it.used.activate() }

    override fun get(name: String): TableValue<Symbol>? = table[name]
}

data class LetSemanticInfo(
    val table: SymbolTableBuilder,
) : SemanticInfo(), SymbolResolver, Table<Symbol> {
    override fun getSymbol(name: String): Symbol? =
        table[name]?.first?.also { it.used.activate() }

    override fun get(name: String): TableValue<Symbol>? = table[name]
}

data class MatchSemanticInfo(
    val table: SymbolTableBuilder,
) : SemanticInfo()

data class TypeSemanticInfo(
    val type: TypePromise
) : SemanticInfo()

fun TypeSystem.getTypeSemanticInfo(name: String) =
    TypeSemanticInfo(getTypePromise(name))

val SemanticInfo.type: ErrorOrType
    get() =
        when (this) {
            is TypeSemanticInfo -> type.type
            else -> TODO()
        }