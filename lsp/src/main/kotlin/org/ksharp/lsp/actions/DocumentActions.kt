package org.ksharp.lsp.actions

import org.ksharp.nodes.NodeData

data class DocumentActions(
    val parseAction: Action<String, List<NodeData>>,
    val semanticTokens: Action<*, List<Int>>
)

fun documentActions(uri: String): DocumentActions {
    val semanticTokensActions = semanticTokenAction()
    val codeModuleErrors = codeModuleErrorsAction {
        trigger {
            +publishSemanticErrorsAction(uri)
        }
    }
    val codeModule = codeModuleAction(uri) {
        trigger {
            +codeModuleErrors
        }
    }
    val parseAction = parseAction {
        trigger {
            +semanticTokensActions
            +codeModule
        }
    }
    return DocumentActions(
        parseAction,
        semanticTokensActions
    )
}
