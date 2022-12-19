package org.ksharp.parser.ksharp

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.nodes.ImportNode
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.NodeData
import org.ksharp.parser.LexerValue
import org.ksharp.parser.build
import org.ksharp.parser.collect
import org.ksharp.parser.thenLoop

fun <L : LexerValue> Iterator<L>.consumeModule(name: String) =
    collect()
        .thenLoop {
            it.consumeImport()
        }.build {
            val location = it.firstOrNull()?.cast<NodeData>()?.location ?: Location.NoProvided
            val imports = it.filterIsInstance<ImportNode>()
            ModuleNode(name, imports, location)
        }