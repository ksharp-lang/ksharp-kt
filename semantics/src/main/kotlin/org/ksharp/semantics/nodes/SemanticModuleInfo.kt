package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.common.cast
import org.ksharp.module.FunctionInfo
import org.ksharp.module.ModuleInfo
import org.ksharp.module.functionInfo
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.expressions.checkFunctionSemantics
import org.ksharp.semantics.expressions.checkInferenceSemantics
import org.ksharp.semantics.typesystem.checkTypesSemantics
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.FunctionType

data class SemanticModuleInfo(
    val name: String,
    val errors: List<Error>,
    val impls: Map<String, Set<String>>,
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
        errors + typeSemantics.errors + moduleSemantics.errors,
        emptyMap(),
        typeSemantics.typeSystem,
        moduleSemantics.abstractions
    )
}

private val FunctionInfo.nameWithArity: String
    get() = if (types.first().representation == "Unit") {
        "$name/${types.size - 1}"
    } else "$name/${types.size}"

private fun List<AbstractionNode<SemanticInfo>>.toFunctionInfoMap() =
    this.asSequence().map {
        val semanticInfo = it.info.cast<AbstractionSemanticInfo>()
        val type = semanticInfo.getInferredType(it.location).valueOrNull!!.cast<FunctionType>()
        functionInfo(it.attributes, it.name, type.arguments)
    }.associateBy { it.nameWithArity }


fun SemanticModuleInfo.toModuleInfo(): ModuleInfo =
    ModuleInfo(
        dependencies = listOf(),
        typeSystem = typeSystem,
        functions = abstractions.toFunctionInfoMap()
    )
