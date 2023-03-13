package org.ksharp.semantics.expressions

import org.ksharp.common.Error
import org.ksharp.semantics.nodes.AbstractionSemanticNode
import org.ksharp.semantics.scopes.Table

data class ModuleFunctionInfo(
    val errors: List<Error>,
    val functionTable: Table<Function>,
    val abstractions: List<AbstractionSemanticNode>
)