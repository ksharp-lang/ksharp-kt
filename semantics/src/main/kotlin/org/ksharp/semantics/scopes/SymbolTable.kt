package org.ksharp.semantics.scopes

import org.ksharp.common.Location
import org.ksharp.common.get
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.TypePromise
import org.ksharp.semantics.nodes.Symbol

class SymbolTableBuilder(private val parent: Table<Symbol>?, collector: ErrorCollector) :
    TableBuilder<Symbol>(parent, collector, "Variable") {
    fun register(name: String, type: TypePromise, location: Location) =
        register(name, Symbol(type), location)

    operator fun get(name: String): TableValue<Symbol>? =
        table.get(name) ?: parent?.get(name)
}

typealias SymbolTable = Table<Symbol>
