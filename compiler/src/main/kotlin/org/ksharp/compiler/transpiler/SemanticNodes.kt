package org.ksharp.compiler.transpiler

import org.ksharp.common.ErrorOrValue
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.SemanticInfo
import org.ksharp.typesystem.types.parameters

val AbstractionNode<SemanticInfo>.parametric: ErrorOrValue<Boolean>
    get() = info.getInferredType(location).map {
        it.parameters.firstOrNull() != null
    }
