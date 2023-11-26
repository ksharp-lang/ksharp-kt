package org.ksharp.module

import org.ksharp.common.Error
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.SemanticInfo

data class CodeArtifact(
    val abstractions: List<AbstractionNode<SemanticInfo>>
)

data class CodeModule(
    val name: String,
    val errors: List<Error>,
    val module: ModuleInfo,
    val artifact: CodeArtifact,
    val traitArtifacts: Map<String, CodeArtifact>,
    val implArtifacts: Map<Impl, CodeArtifact>,
)
