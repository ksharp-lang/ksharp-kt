package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.semantics.expressions.FunctionTable
import org.ksharp.semantics.typesystem.TypeVisibilityTable
import org.ksharp.typesystem.TypeSystem

data class ModuleSemanticNode(
    val name: String,
    val errors: List<Error>,
    val typeSystemTable: TypeVisibilityTable,
    val typeSystem: TypeSystem,
    val functionTable: FunctionTable = FunctionTable(null, mapOf())
)