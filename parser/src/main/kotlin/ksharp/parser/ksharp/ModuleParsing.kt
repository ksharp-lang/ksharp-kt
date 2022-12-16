package ksharp.parser.ksharp

import ksharp.nodes.ImportNode
import ksharp.nodes.ModuleNode
import ksharp.nodes.NodeData
import ksharp.parser.*
import org.ksharp.common.Location
import org.ksharp.common.cast

fun <L : LexerValue> Iterator<L>.consumeModule(name: String) =
    collect()
        .thenLoopIndexed { it, index ->
            if (index != 0) it.consume(KSharpTokenType.NewLine)
                .consume { consumeImport() }
                .build { it.last().cast() }
            else consumeImport()
        }.build {
            val location = it.firstOrNull()?.cast<NodeData>()?.location ?: Location.NoProvided
            val imports = it.filterIsInstance<ImportNode>()
            ModuleNode(name, imports, location)
        }