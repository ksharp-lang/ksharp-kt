package org.ksharp.semantics.scopes

import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.TypePromise

enum class FunctionVisibility {
    Internal,
    Public
}

data class Function(
    val visibility: FunctionVisibility,
    val name: String,
    val type: List<TypePromise>,
)

class FunctionTableBuilder(collector: ErrorCollector) :
    TableBuilder<Function>(null, collector, "Function")

val TableValue<Function>.isInternal get() = first.isInternal

val TableValue<Function>.isPublic get() = first.isPublic

val Function.isInternal get() = this.visibility == FunctionVisibility.Internal
val Function.isPublic get() = this.visibility == FunctionVisibility.Public

typealias FunctionTable = TableImpl<Function>