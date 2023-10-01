package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.TraitType

data class ModuleTypeSystemInfo(
    val errors: List<Error>,
    val typeSystem: TypeSystem,
    val traits: List<TraitType>
)
