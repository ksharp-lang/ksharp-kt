package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.typesystem.TypeSystem

data class ModuleTypeSystemInfo(
    val errors: List<Error>,
    val typeSystem: TypeSystem,
)