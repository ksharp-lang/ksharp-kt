package org.ksharp.lsp.actions

import org.ksharp.common.Error
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.NodeData
import org.ksharp.parser.ksharp.toModuleNode
import org.ksharp.semantics.nodes.toCodeModule

const val SemanticModuleInfoAction = "SemanticModuleInfoAction"
fun codeModuleErrorsAction(moduleName: String, builder: ActionsGraphBuilder<List<Error>>) =
    action<List<NodeData>, List<Error>>(
        SemanticModuleInfoAction,
        emptyList<Error>()
    ) {
        execution { _, nodes ->
            nodes.asSequence().toModuleNode(moduleName)
                .toCodeModule(preludeModule)
                .errors
        }
        graphBuilder(builder)
    }
