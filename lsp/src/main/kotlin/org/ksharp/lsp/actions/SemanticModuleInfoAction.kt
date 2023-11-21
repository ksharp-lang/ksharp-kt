package org.ksharp.lsp.actions

import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.NodeData
import org.ksharp.parser.ksharp.toModuleNode
import org.ksharp.semantics.nodes.SemanticModuleInfo
import org.ksharp.semantics.nodes.toSemanticModuleInfo
import org.ksharp.semantics.nodes.toSemanticModuleInterface

const val SemanticModuleInfoAction = "SemanticModuleInfoAction"
fun semanticModuleInfoAction(moduleName: String, builder: ActionsGraphBuilder<SemanticModuleInfo>) =
    action<List<NodeData>, SemanticModuleInfo>(
        SemanticModuleInfoAction,
        SemanticModuleInfo(
            moduleName,
            listOf(),
            preludeModule.typeSystem,
            setOf(),
            emptyMap(),
            emptyMap(),
            listOf(),
        )
    ) {
        execution { _, nodes ->
            nodes.asSequence().toModuleNode(moduleName)
                .toSemanticModuleInterface(preludeModule)
                .toSemanticModuleInfo()
        }
        graphBuilder(builder)
    }
