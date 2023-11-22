package org.ksharp.compiler.transpiler

import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.SemanticInfo
import org.ksharp.nodes.semantic.SemanticNode
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.parameters

val SemanticNode<SemanticInfo>.type: ErrorOrType
    get() =
        this.info.getInferredType(Location.NoProvided)


val AbstractionNode<SemanticInfo>.parametric: ErrorOrValue<Boolean>
    get() = type.map {
        it.parameters.firstOrNull() != null
    }
