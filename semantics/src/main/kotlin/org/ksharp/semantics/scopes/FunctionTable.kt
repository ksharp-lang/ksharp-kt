package org.ksharp.semantics.scopes

import org.ksharp.common.Table
import org.ksharp.common.TableValue
import org.ksharp.nodes.semantic.TypePromise
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute

data class Function(
    val attributes: Set<Attribute>,
    val name: String,
    val type: List<TypePromise>,
)

class FunctionTableBuilder(collector: ErrorCollector) :
    TableBuilderImpl<Function>(null, collector, "Function")

val TableValue<Function>.isInternal get() = first.isInternal

val TableValue<Function>.isPublic get() = first.isPublic

val Function.isInternal get() = attributes.contains(CommonAttribute.Internal)
val Function.isPublic get() = attributes.contains(CommonAttribute.Public)

typealias FunctionTable = Table<Function>
