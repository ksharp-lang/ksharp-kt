package org.ksharp.lsp.actions

import org.ksharp.nodes.NodeData
import org.ksharp.parser.ksharp.parseModuleAsNodeSequence

val ParseAction = ActionId<List<NodeData>>("ParseAction")
fun ActionCatalog.parseAction(builder: ActionsGraphBuilder<List<NodeData>>) = action<String, List<NodeData>>(
    ParseAction,
    listOf()
) {
    execution { _, content ->
        content.parseModuleAsNodeSequence()
    }
    graphBuilder(builder)
}
