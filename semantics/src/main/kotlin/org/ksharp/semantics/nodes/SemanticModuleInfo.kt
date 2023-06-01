package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.expressions.checkFunctionSemantics
import org.ksharp.semantics.expressions.checkInferenceSemantics
import org.ksharp.semantics.expressions.toFunctionInfoMap
import org.ksharp.semantics.typesystem.checkTypesSemantics
import org.ksharp.typesystem.TypeSystem

data class SemanticModuleInfo(
    val name: String,
    val errors: List<Error>,
    val typeSystem: TypeSystem,
    val abstractions: List<AbstractionNode<SemanticInfo>>
)

fun ModuleNode.toSemanticModuleInfo(preludeModule: ModuleInfo): SemanticModuleInfo {
    val typeSemantics = this.checkTypesSemantics(preludeModule)
    val moduleSemantics = this.checkFunctionSemantics(typeSemantics)
        .checkInferenceSemantics(typeSemantics, preludeModule)
    return SemanticModuleInfo(
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

fun SemanticModuleInfo.toModuleInfo(): ModuleInfo =
    ModuleInfo(
        dependencies = listOf(),
        typeSystem = typeSystem,
        functions = abstractions.toFunctionInfoMap()
    )
