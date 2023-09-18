package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.common.cast
import org.ksharp.module.FunctionInfo
import org.ksharp.module.ModuleInfo
import org.ksharp.module.functionInfo
import org.ksharp.module.traitInfo
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.expressions.checkFunctionSemantics
import org.ksharp.semantics.expressions.checkInferenceSemantics
import org.ksharp.semantics.typesystem.checkTypesSemantics
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.TraitType

data class SemanticModuleInfo(
    val name: String,
    val errors: List<Error>,
    val typeSystem: TypeSystem,
    val traits: List<TraitType>,
    val traitsAbstractions: Map<String, List<AbstractionNode<SemanticInfo>>>,
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
        typeSemantics.typeSystem,
        typeSemantics.traits,
        moduleSemantics.traitsAbstractions,
        moduleSemantics.abstractions,
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


fun SemanticModuleInfo.toModuleInfo(): ModuleInfo {
    return ModuleInfo(
        dependencies = listOf(),
        typeSystem = typeSystem,
        functions = abstractions.toFunctionInfoMap(),
        traits = traits.associate {
            it.name to traitInfo(it.name, it.methods.values.associate { method ->
                "${method.name}/${method.arguments.size}" to method
            }, (traitsAbstractions[it.name] ?: emptyList()).toFunctionInfoMap())
        }
    )
}
