package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.nodes.semantic.AbstractionNode

data class ModuleFunctionInfo(
    val errors: List<Error>,
    val abstractions: List<AbstractionNode<SemanticInfo>>
)