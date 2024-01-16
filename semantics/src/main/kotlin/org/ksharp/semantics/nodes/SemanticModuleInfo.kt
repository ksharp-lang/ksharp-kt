package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.common.cast
import org.ksharp.module.*
import org.ksharp.nodes.ImportNode
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.SemanticInfo
import org.ksharp.semantics.expressions.checkFunctionSemantics
import org.ksharp.semantics.expressions.checkInferenceSemantics
import org.ksharp.semantics.typesystem.checkTypesSemantics
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.FunctionType

data class SemanticModuleInfo internal constructor(
    val name: String,
    val errors: List<Error>,
    val dependencies: Map<String, String>,
    val typeSystem: TypeSystem,
    val impls: Set<Impl>,
    val traitsAbstractions: Map<String, List<AbstractionNode<SemanticInfo>>>,
    val implAbstractions: Map<Impl, List<AbstractionNode<SemanticInfo>>>,
    val abstractions: List<AbstractionNode<SemanticInfo>>
)

data class SemanticModuleInterface internal constructor(
    val name: String,
    val errors: List<Error>,
    val dependencies: Map<String, ModuleInfo>,
    val preludeModule: ModuleInfo,
    val typeSystemInfo: ModuleTypeSystemInfo,
    val functionInfo: ModuleFunctionInfo
)

fun interface ModuleInfoLoader {
    fun load(name: String, from: String): ModuleInfo?

}

internal fun ModuleNode.toSemanticModuleInterface(
    preludeModule: ModuleInfo,
    loader: ModuleInfoLoader
): SemanticModuleInterface {
    val dependencies = imports
        .asSequence()
        .mapNotNull {
            loader.load(it.moduleName, name)?.let { module ->
                it.key to module
            }
        }.toMap()
    val typeSemantics = this.checkTypesSemantics(preludeModule, dependencies)
    val moduleSemantics = this.checkFunctionSemantics(typeSemantics)
    return SemanticModuleInterface(
        name.let {
            val ix = name.indexOf(".")
            if (ix != -1) name.substring(0, ix)
            else name
        },
        errors + typeSemantics.errors + moduleSemantics.errors,
        dependencies,
        preludeModule,
        typeSemantics,
        moduleSemantics
    )
}

internal fun SemanticModuleInterface.toSemanticModuleInfo(imports: List<ImportNode>): SemanticModuleInfo {
    val functionInfo = checkInferenceSemantics()
    return SemanticModuleInfo(
        name.let {
            val ix = name.indexOf(".")
            if (ix != -1) name.substring(0, ix)
            else name
        },
        errors + functionInfo.errors,
        imports.asSequence().map {
            it.key to it.moduleName
        }.toMap(),
        typeSystemInfo.typeSystem,
        typeSystemInfo.impls.keys,
        functionInfo.traitsAbstractions,
        functionInfo.implAbstractions,
        functionInfo.abstractions,
    )
}

internal fun SemanticModuleInfo.toCodeModule() =
    CodeModule(
        name,
        errors,
        ModuleInfo(
            dependencies = dependencies,
            typeSystem = typeSystem,
            functions = abstractions.toFunctionInfoMap(),
            impls = impls,
        ),
        CodeArtifact(abstractions),
        traitsAbstractions.mapValues { CodeArtifact(it.value) },
        implAbstractions.mapValues { CodeArtifact(it.value) }
    )

private fun List<AbstractionNode<SemanticInfo>>.toFunctionInfoMap() =
    this.asSequence().map {
        val semanticInfo = it.info
        val type = semanticInfo.getInferredType(it.location).valueOrNull!!.cast<FunctionType>()
        functionInfo(it.attributes, it.name, type.arguments)
    }.associateBy { it.nameWithArity }


fun ModuleNode.toCodeModule(preludeModule: ModuleInfo, loader: ModuleInfoLoader): CodeModule =
    toSemanticModuleInterface(preludeModule, loader).toSemanticModuleInfo(imports).toCodeModule()
