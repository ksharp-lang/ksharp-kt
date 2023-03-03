package org.ksharp.semantics.typesystem

import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.scopes.TableBuilder
import org.ksharp.semantics.scopes.TableValue

enum class TypeVisibility {
    Internal,
    Public
}

class TypeVisibilityTableBuilder(collector: ErrorCollector) :
    TableBuilder<TypeVisibility>(collector, "Type")

val TableValue<TypeVisibility>.isInternal get() = first.isInternal

val TableValue<TypeVisibility>.isPublic get() = first.isPublic

val TypeVisibility.isInternal get() = this == TypeVisibility.Internal
val TypeVisibility.isPublic get() = this == TypeVisibility.Public
