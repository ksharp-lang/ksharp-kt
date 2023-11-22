package org.ksharp.ir.transform

import org.ksharp.nodes.semantic.SemanticInfo
import org.ksharp.nodes.semantic.SemanticNode
import org.ksharp.typesystem.types.Type

val SemanticNode<SemanticInfo>.inferredType: Type
    get() =
        info.getInferredType(location).valueOrNull!!
