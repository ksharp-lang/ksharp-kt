package org.ksharp.semantics.expressions

import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.TypePromise
import org.ksharp.semantics.scopes.Table
import org.ksharp.semantics.scopes.TableBuilder
import org.ksharp.semantics.scopes.TableValue

enum class FunctionVisibility {
    Internal,
    Public
}

data class Function(
    val visibility: FunctionVisibility,
    val name: String,
    val type: TypePromise
)

class FunctionTableBuilder(collector: ErrorCollector) :
    TableBuilder<Function, Unit>(null, collector, "Function")

val TableValue<Function>.isInternal get() = first.isInternal

val TableValue<Function>.isPublic get() = first.isPublic

val Function.isInternal get() = this.visibility == FunctionVisibility.Internal
val Function.isPublic get() = this.visibility == FunctionVisibility.Public

typealias FunctionTable = Table<Function, Unit>
