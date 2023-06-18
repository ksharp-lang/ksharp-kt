package org.ksharp.parser.ksharp

import org.ksharp.common.*
import org.ksharp.nodes.*
import org.ksharp.parser.*

data class InvalidToken(
    val text: String,
    val type: TokenType,
    val location: Location
)

data class InvalidNode(
    val tokens: List<InvalidToken>,
    val error: Error
) : NodeData() {
    override val locations: NodeLocations
        get() = NoLocationsDefined
    override val children: Sequence<NodeData>
        get() = emptySequence()
    override val location: Location
        get() = Location.NoProvided
}

private fun KSharpLexerIterator.consumeInvalidTokens(error: Error): KSharpParserResult =
    collect()
        .thenLoop { t ->
            t.consume({
                it.type != KSharpTokenType.EndBlock
            }).build { it.first() }
        }.thenLoop { t ->
            t.consume({
                it.type != KSharpTokenType.BeginBlock
            }).build { it.first() }
        }.build {
            InvalidNode(
                it.asSequence()
                    .filter { i ->
                        when (i.cast<Token>().type) {
                            KSharpTokenType.BeginBlock, BaseTokenType.NewLine, KSharpTokenType.EndBlock -> false
                            else -> true
                        }
                    }
                    .map { i ->
                        val t = i.cast<Token>()
                        InvalidToken(
                            t.text,
                            t.type,
                            t.location
                        )
                    }.toList(),
                error
            )
        }

private fun KSharpParserResult.consumeInvalidTokens(state: KSharpLexerState): KSharpParserResult =
    mapLeft { e ->
        state.lastError.set(e.error)
        e
    }.or { l -> l.consumeInvalidTokens(state.lastError.get()!!) }

private fun KSharpLexerIterator.consumeModuleNodesLogic(): KSharpConsumeResult =
    collect()
        .thenLoop { then ->
            then.consumeBlock {
                it.consumeImport()
                    .or { l -> l.consumeFunctionTypeDeclaration() }
                    .or { l -> l.consumeTypeDeclaration() }
                    .or { l -> l.consumeAnnotation() }
                    .or { l -> l.consumeFunction() }
                    .consumeInvalidTokens(state.value)
            }.flatMapLeft {
                if (it.collection.size() != 0) {
                    Either.Right(ParserValue(it.collection.build().first(), it.remainTokens))
                } else Either.Left(it)
            }.cast<KSharpParserResult>()
        }

fun Sequence<NodeData>.toModuleNode(name: String): ModuleNode {
    val location = firstOrNull()?.cast<NodeData>()?.location ?: Location.NoProvided
    val imports = filterIsInstance<ImportNode>().toList()
    val types = filter { n -> n is TypeNode || n is TraitNode }.toList()
    val typeDeclarations = filterIsInstance<TypeDeclarationNode>().toList()
    val functions = filterIsInstance<FunctionNode>().toList()
    val errors = asSequence().filterIsInstance<InvalidNode>().map {
        it.error
    }.toList()
    return ModuleNode(name, imports, types, typeDeclarations, functions, errors, location)
}

fun KSharpLexerIterator.consumeModule(name: String): ParserResult<ModuleNode, KSharpLexerState> =
    consumeModuleNodesLogic().build {
        it.asSequence().filterIsInstance<NodeData>().toModuleNode(name)
    }


fun KSharpLexerIterator.consumeModuleNodes(): List<NodeData> =
    consumeModuleNodesLogic().build {
        it.filterIsInstance<NodeData>()
    }.map { it.value }.valueOrNull ?: listOf()
