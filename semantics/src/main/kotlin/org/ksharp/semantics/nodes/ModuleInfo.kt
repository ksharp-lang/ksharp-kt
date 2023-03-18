package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.semantics.scopes.FunctionTable
import org.ksharp.semantics.scopes.TypeVisibilityTable
import org.ksharp.typesystem.TypeSystem

data class ModuleInfo(
    val name: String,
    val errors: List<Error>,
    val typeSystemTable: TypeVisibilityTable,
    val typeSystem: TypeSystem,
    val functionTable: FunctionTable = FunctionTable(null, mapOf())
)