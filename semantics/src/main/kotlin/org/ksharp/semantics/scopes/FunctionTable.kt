package org.ksharp.semantics.scopes

import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.nodes.TypePromise
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute

data class Function(
    val attributes: Set<Attribute>,
    val name: String,
    val type: List<TypePromise>,
)

class FunctionTableBuilder(collector: ErrorCollector) :
    TableBuilder<Function>(null, collector, "Function")

val TableValue<Function>.isInternal get() = first.isInternal

val TableValue<Function>.isPublic get() = first.isPublic

val Function.isInternal get() = attributes.contains(CommonAttribute.Internal)
val Function.isPublic get() = attributes.contains(CommonAttribute.Public)

typealias FunctionTable = TableImpl<Function>
