package org.ksharp.lsp.actions

import org.ksharp.nodes.NodeData
import java.net.URI
import kotlin.io.path.name
import kotlin.io.path.toPath

data class DocumentActions(
    val parseAction: Action<String, List<NodeData>>,
    val semanticTokens: Action<*, List<Int>>
)

fun documentActions(uri: String): DocumentActions {
    val semanticTokensActions = semanticTokenAction()
    val semanticModuleInfoAction = semanticModuleInfoAction(URI(uri).toPath().fileName.name) {
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