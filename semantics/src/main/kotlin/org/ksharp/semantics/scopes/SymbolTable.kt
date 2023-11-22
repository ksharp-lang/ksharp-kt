package org.ksharp.semantics.scopes

import org.ksharp.common.Location
import org.ksharp.common.Table
import org.ksharp.common.TableValue
import org.ksharp.common.get
import org.ksharp.nodes.semantic.Symbol
import org.ksharp.nodes.semantic.TypePromise
import org.ksharp.semantics.errors.ErrorCollector

class SymbolTableBuilder(private val parent: Table<Symbol>?, collector: ErrorCollector) :
    TableBuilderImpl<Symbol>(parent, collector, "Variable") {
    fun register(name: String, type: TypePromise, location: Location) =
        register(name, Symbol(name, type), location)

    override operator fun get(name: String): TableValue<Symbol>? =
        table.get(name) ?: parent?.get(name)
}

typealias SymbolTable = Table<Symbol>
