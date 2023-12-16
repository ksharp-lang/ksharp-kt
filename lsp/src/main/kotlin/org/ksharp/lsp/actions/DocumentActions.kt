package org.ksharp.lsp.actions

import org.ksharp.common.Error
import org.ksharp.nodes.NodeData

data class DocumentActions(
    val parseAction: Action<String, List<NodeData>>,
    val semanticTokens: Action<*, List<Int>>,
    val publishErrors: Action<List<Error>, Boolean>,
)

fun documentActions(uri: String): DocumentActions {
    val publishErrorsAction = publishSemanticErrorsAction(uri)
    val semanticTokensActions = semanticTokenAction()
    val codeModuleErrors = codeModuleErrorsAction {
        trigger {
            +publishErrorsAction
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
        semanticTokensActions,
        publishErrorsAction
    )
}
