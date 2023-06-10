package org.ksharp.lsp.actions

import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.NodeData
import org.ksharp.parser.ksharp.toModuleNode
import org.ksharp.semantics.nodes.SemanticModuleInfo
import org.ksharp.semantics.nodes.toSemanticModuleInfo

const val SemanticModuleInfoAction = "SemanticModuleInfoAction"
fun semanticModuleInfoAction(moduleName: String, builder: ActionsGraphBuilder<SemanticModuleInfo>) =
    action<List<NodeData>, SemanticModuleInfo>(
        SemanticModuleInfoAction,
        SemanticModuleInfo(
            moduleName,
            listOf(),
            preludeModule.typeSystem,
            listOf()
        )
    ) {
        execution { _, nodes ->
            nodes.toModuleNode(moduleName)
                .toSemanticModuleInfo(preludeModule)
        }
        graphBuilder(builder)
    }