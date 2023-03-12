package org.ksharp.semantics.expressions

import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.TypePromise
import org.ksharp.semantics.scopes.Table
import org.ksharp.semantics.scopes.TableBuilder

enum class RecordSize(val size: Int) {
    Single(1),
    Double(2);
}

class SymbolTableBuilder(parent: Table<TypePromise>?, collector: ErrorCollector) :
    TableBuilder<TypePromise>(parent, collector, "Variable")

typealias SymbolTable = Table<TypePromise>