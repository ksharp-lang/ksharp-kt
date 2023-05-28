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
                    .or { l -> l.consumeFunction() }
                    .or { l -> l.consumeAnnotation() }
            }
        }.build {
            val location = it.firstOrNull()?.cast<NodeData>()?.location ?: Location.NoProvided
            val imports = it.filterIsInstance<ImportNode>()
            val types = it
                .filter { n -> n is TypeNode || n is TraitNode }
                .map { n -> n as NodeData }
            val typeDeclarations = it.filterIsInstance<TypeDeclarationNode>()
            val functions = it.filterIsInstance<FunctionNode>()
            ModuleNode(name, imports, types, typeDeclarations, functions, location)
        }
