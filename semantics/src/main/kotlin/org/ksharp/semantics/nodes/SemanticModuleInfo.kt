package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.common.cast
import org.ksharp.module.FunctionInfo
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.module.functionInfo
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.expressions.checkFunctionSemantics
import org.ksharp.semantics.expressions.checkInferenceSemantics
import org.ksharp.semantics.typesystem.checkTypesSemantics
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.isUnitType

data class SemanticModuleInfo(
    val name: String,
    val errors: List<Error>,
    val typeSystem: TypeSystem,
    val impls: Set<Impl>,
    val traitsAbstractions: Map<String, List<AbstractionNode<SemanticInfo>>>,
    val implAbstractions: Map<Impl, List<AbstractionNode<SemanticInfo>>>,
    val abstractions: List<AbstractionNode<SemanticInfo>>
)

data class SemanticModuleInterface(
    val name: String,
    val errors: List<Error>,
    val preludeModule: ModuleInfo,
    val typeSystemInfo: ModuleTypeSystemInfo,
    val functionInfo: ModuleFunctionInfo
)

fun ModuleNode.toSemanticModuleInterface(preludeModule: ModuleInfo): SemanticModuleInterface {
    val typeSemantics = this.checkTypesSemantics(preludeModule)
    val moduleSemantics = this.checkFunctionSemantics(typeSemantics)
    return SemanticModuleInterface(
        name.let {
            val ix = name.indexOf(".")
            if (ix != -1) name.substring(0, ix)
            else name
        },
        errors + typeSemantics.errors + moduleSemantics.errors,
        preludeModule,
        typeSemantics,
        moduleSemantics
    )
}

fun SemanticModuleInterface.toSemanticModuleInfo(): SemanticModuleInfo {
    val functionInfo = checkInferenceSemantics()
    return SemanticModuleInfo(
        name.let {
            val ix = name.indexOf(".")
            if (ix != -1) name.substring(0, ix)
            else name
        },
        errors + functionInfo.errors,
        typeSystemInfo.typeSystem,
        typeSystemInfo.impls.keys,
        functionInfo.traitsAbstractions,
        functionInfo.implAbstractions,
        functionInfo.abstractions,
    )
}

val FunctionInfo.nameWithArity: String
    get() = when (val size = types.size) {
        2 -> if (types.first().isUnitType) 0 else 1
        else -> size - 1
    }.let { "$name/$it" }

private fun List<AbstractionNode<SemanticInfo>>.toFunctionInfoMap() =
    this.asSequence().map {
        val semanticInfo = it.info.cast<AbstractionSemanticInfo>()
        val type = semanticInfo.getInferredType(it.location).valueOrNull!!.cast<FunctionType>()
        functionInfo(it.attributes, it.name, type.arguments)
    }.associateBy { it.nameWithArity }


fun SemanticModuleInfo.toModuleInfo(): ModuleInfo {
    return ModuleInfo(
        dependencies = mapOf(),
        typeSystem = typeSystem,
        functions = abstractions.toFunctionInfoMap(),
        impls = impls,
    )
}
