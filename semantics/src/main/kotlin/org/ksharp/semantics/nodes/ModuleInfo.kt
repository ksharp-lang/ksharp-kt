package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.expressions.checkFunctionSemantics
import org.ksharp.semantics.expressions.checkInferenceSemantics
import org.ksharp.semantics.typesystem.checkTypesSemantics
import org.ksharp.typesystem.TypeSystem

data class ModuleInfo(
    val name: String,
    val errors: List<Error>,
    val typeSystem: TypeSystem,
    val abstractions: List<AbstractionNode<SemanticInfo>>
)

fun ModuleNode.toModuleInfo(): ModuleInfo {
    val typeSemantics = this.checkTypesSemantics()
    val moduleSemantics = this.checkFunctionSemantics(typeSemantics)
        .checkInferenceSemantics(typeSemantics)
    return ModuleInfo(
        name,
        typeSemantics.errors + moduleSemantics.errors,
        typeSemantics.typeSystem,
        moduleSemantics.abstractions
    )
}