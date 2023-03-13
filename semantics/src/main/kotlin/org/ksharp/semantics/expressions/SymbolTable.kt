package org.ksharp.semantics.expressions

import org.ksharp.common.Flag
import org.ksharp.common.Location
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.TypePromise
import org.ksharp.semantics.scopes.Table
import org.ksharp.semantics.scopes.TableBuilder

data class Symbol(val type: TypePromise) {
    val used: Flag = Flag()
}

class SymbolTableBuilder(parent: Table<Symbol>?, collector: ErrorCollector) :
    TableBuilder<Symbol>(parent, collector, "Variable") {
    fun register(name: String, type: TypePromise, location: Location) =
        register(name, Symbol(type), location)
}

typealias SymbolTable = Table<TypePromise>