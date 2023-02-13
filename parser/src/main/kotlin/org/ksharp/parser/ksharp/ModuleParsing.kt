package org.ksharp.parser.ksharp

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.nodes.*
import org.ksharp.parser.*

fun KSharpLexerIterator.consumeModule(name: String): ParserResult<ModuleNode, KSharpLexerState> =
    collect()
        .thenLoop { then ->
            then.consumeBlock {
                it.consumeImport()
                    .or { l -> l.consumeFunctionTypeDeclaration() }
                    .or { l -> l.consumeTypeDeclaration() }
            }
        }.build {
            val location = it.firstOrNull()?.cast<NodeData>()?.location ?: Location.NoProvided
            val imports = it.filterIsInstance<ImportNode>().associateBy { t -> t.key }
            val types = it.filterIsInstance<TypeNode>().associateBy { t -> t.name }
            val typeDeclarations = it.filterIsInstance<TypeDeclarationNode>().associateBy { t -> t.name }
            val functions = it.filterIsInstance<FunctionNode>().associateBy { t -> t.name }
            ModuleNode(name, imports, types, typeDeclarations, functions, location)
        }