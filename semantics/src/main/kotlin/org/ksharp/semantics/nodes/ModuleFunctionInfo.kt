package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.module.Impl
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.SemanticInfo

data class ModuleFunctionInfo(
    val errors: List<Error>,
    val abstractions: List<AbstractionNode<SemanticInfo>>,
    val traitsAbstractions: Map<String, List<AbstractionNode<SemanticInfo>>>,
    val implAbstractions: Map<Impl, List<AbstractionNode<SemanticInfo>>>,
)
