package org.ksharp.semantics.scopes

import org.ksharp.semantics.errors.ErrorCollector

enum class TypeVisibility {
    Internal,
    Public
}

class TypeVisibilityTableBuilder(collector: ErrorCollector) :
    TableBuilder<TypeVisibility>(null, collector, "Type")

val TableValue<TypeVisibility>.isInternal get() = first.isInternal

val TableValue<TypeVisibility>.isPublic get() = first.isPublic

val TypeVisibility.isInternal get() = this == TypeVisibility.Internal
val TypeVisibility.isPublic get() = this == TypeVisibility.Public

typealias TypeVisibilityTable = TableImpl<TypeVisibility>