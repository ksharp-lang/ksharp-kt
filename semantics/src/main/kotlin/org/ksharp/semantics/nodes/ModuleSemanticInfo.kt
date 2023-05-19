package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.expressions.checkFunctionSemantics
import org.ksharp.semantics.expressions.checkInferenceSemantics
import org.ksharp.semantics.typesystem.checkTypesSemantics
import org.ksharp.typesystem.TypeSystem

data class ModuleSemanticInfo(
    val name: String,
    val errors: List<Error>,
    val typeSystem: TypeSystem,
    val abstractions: List<AbstractionNode<SemanticInfo>>
)

fun ModuleNode.toModuleSemanticInfo(): ModuleSemanticInfo {
    val typeSemantics = this.checkTypesSemantics()
    val moduleSemantics = this.checkFunctionSemantics(typeSemantics)
        .checkInferenceSemantics(typeSemantics)
    return ModuleSemanticInfo(
        name.let {
            val ix = name.indexOf(".")
            if (ix != -1) name.substring(0, ix)
            else name
        },
        typeSemantics.errors + moduleSemantics.errors,
        typeSemantics.typeSystem,
        moduleSemantics.abstractions
    )
}