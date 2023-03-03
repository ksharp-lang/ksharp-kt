package org.ksharp.semantics.scopes

import org.ksharp.common.Error
import org.ksharp.semantics.typesystem.TypeVisibility
import org.ksharp.typesystem.TypeSystem

class ModuleSemanticNode(
    val errors: List<Error>,
    val typeSystemTable: Table<TypeVisibility>,
    val typeSystem: TypeSystem
)