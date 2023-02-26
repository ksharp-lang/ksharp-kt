package org.ksharp.semantics.typesystem

import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.scopes.TableBuilder

enum class TypeVisibility {
    Internal,
    Public
}

class TypeVisibilityTableBuilder(private val collector: ErrorCollector) :
    TableBuilder<TypeVisibility>(collector, "Type")

val TypeVisibility.isInternal get() = this == TypeVisibility.Internal
val TypeVisibility.isPublic get() = this == TypeVisibility.Public
