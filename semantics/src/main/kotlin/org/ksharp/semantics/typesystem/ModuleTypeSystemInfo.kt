package org.ksharp.semantics.typesystem

import org.ksharp.common.Error
import org.ksharp.typesystem.TypeSystem

data class ModuleTypeSystemInfo(
    val errors: List<Error>,
    val typeSystemTable: TypeVisibilityTable,
    val typeSystem: TypeSystem,
)