package org.ksharp.parser.ksharp

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.nodes.ImportNode
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.NodeData
import org.ksharp.parser.*

fun KSharpLexerIterator.consumeModule(name: String): ParserResult<ModuleNode, KSharpLexerState> =
    collect()
        .thenLoop { then ->
            then.consumeBlock {
                it.consumeImport()
                    .or { consumeTypeDeclaration() }
            }
        }.build {
            val location = it.firstOrNull()?.cast<NodeData>()?.location ?: Location.NoProvided
            val imports = it.filterIsInstance<ImportNode>()
            ModuleNode(name, imports, location)
        }