package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.semantics.scopes.TypeVisibilityTable
import org.ksharp.typesystem.TypeSystem

data class ModuleTypeSystemInfo(
    val errors: List<Error>,
    val typeSystemTable: TypeVisibilityTable,
    val typeSystem: TypeSystem,
)