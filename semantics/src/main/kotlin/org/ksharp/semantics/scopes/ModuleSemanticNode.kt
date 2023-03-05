package org.ksharp.semantics.scopes

import org.ksharp.common.Error
import org.ksharp.semantics.typesystem.TypeVisibilityTable
import org.ksharp.typesystem.TypeSystem

class ModuleSemanticNode(
    val errors: List<Error>,
    val typeSystemTable: TypeVisibilityTable,
    val typeSystem: TypeSystem
)