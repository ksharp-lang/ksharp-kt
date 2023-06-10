package org.ksharp.lsp.actions

import org.ksharp.nodes.NodeData

data class DocumentActions(
    val parseAction: Action<String, List<NodeData>>,
    val semanticTokens: Action<*, List<Int>>
)

fun documentActions(uri: String): DocumentActions {
    val semanticTokensActions = semanticTokenAction()
    val semanticModuleInfoAction = semanticModuleInfoAction(uri) {
        trigger {
            +publishSemanticErrorsAction(uri)
        }
    }
    val parseAction = parseAction {
        trigger {
            +semanticTokensActions
            +semanticModuleInfoAction
        }
    }
    return DocumentActions(
        parseAction,
        semanticTokensActions
    )
}