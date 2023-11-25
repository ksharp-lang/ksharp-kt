package org.ksharp.module

import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.SemanticInfo

data class CodeArtifact(
    val abstractions: List<AbstractionNode<SemanticInfo>>
)

data class CodeModule(
    val module: ModuleInfo,
    val artifact: CodeArtifact,
    val traitArtifacts: Map<String, CodeArtifact>,
    val implArtifacts: Map<Impl, CodeArtifact>,
)
