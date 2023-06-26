package org.ksharp.ir.transform

import org.ksharp.nodes.semantic.SemanticNode
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.typesystem.types.Type

val SemanticNode<SemanticInfo>.inferredType: Type
    get() =
        info.getInferredType(location).valueOrNull!!
