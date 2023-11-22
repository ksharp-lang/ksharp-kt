package org.ksharp.nodes.semantic

import org.ksharp.common.Flag
import org.ksharp.common.Table
import org.ksharp.common.TableValue

fun interface SymbolResolver {
    fun getSymbol(name: String): Symbol?
}

data class Symbol(val name: String, val type: TypePromise) : SemanticInfo() {
    val used: Flag = Flag()
}

data class SymbolTableSemanticInfo(
    private val table: Table<Symbol>,
) : SemanticInfo(), SymbolResolver, Table<Symbol> {
    override fun getSymbol(name: String): Symbol? =
        table[name]?.first?.also { it.used.activate() }

    override fun get(name: String): TableValue<Symbol>? = table[name]
}
