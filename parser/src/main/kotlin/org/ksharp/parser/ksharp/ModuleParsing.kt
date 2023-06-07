package org.ksharp.parser.ksharp

import org.ksharp.common.Error
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.nodes.*
import org.ksharp.parser.*

data class InvalidToken(
    val text: String,
    val type: TokenType,
    val location: Location
)

data class InvalidNode(
    val token: List<InvalidToken>,
    val error: Error
) : NodeData() {
    override val locations: NodeLocations
        get() = NoLocationsDefined
    override val children: Sequence<NodeData>
        get() = emptySequence()
    override val location: Location
        get() = node.location
}

private fun KSharpConsumeResult.consumeInvalidTokens(error: Error): KSharpParserResult =
    thenInBlock { cb ->
        cb.collect()
            .enableDiscardBlockAndNewLineTokens { tl ->
                tl.thenLoop { l ->
                    l.consume({
                        it.type != KSharpTokenType.EndBlock
                    }).build { it.first() }
                }.build {
                    InvalidNode(
                        it.map { i ->
                            val t = i.cast<Token>()
                            InvalidToken(
                                t.text,
                                t.type,
                                t.location
                            )
                        },
                        error
                    )
                }
            }
    }.discardBlanks()
        .build {
            it.first().cast()
        }

private fun KSharpLexerIterator.consumeModuleNodesLogic(): KSharpConsumeResult =
    collect()
        .thenLoop { then ->
            then.lookAHead { lookAHead ->
                lookAHead.consumeBlock {
                    it.consumeImport()
                        .or { l -> l.consumeFunctionTypeDeclaration() }
                        .or { l -> l.consumeTypeDeclaration() }
                        .or { l -> l.consumeFunction() }
                        .or { l -> l.consumeAnnotation() }
                }.asLookAHeadResult()
            }.mapLeft {
                state.value.lastError.set(it.error)
                it
            }.orCollect { l -> l.consumeInvalidTokens(state.value.lastError.get()!!) }
        }

fun KSharpLexerIterator.consumeModule(name: String): ParserResult<ModuleNode, KSharpLexerState> =
    consumeModuleNodesLogic().build {
        val location = it.firstOrNull()?.cast<NodeData>()?.location ?: Location.NoProvided
        val imports = it.filterIsInstance<ImportNode>()
        val types = it
            .filter { n -> n is TypeNode || n is TraitNode }
            .map { n -> n as NodeData }
        val typeDeclarations = it.filterIsInstance<TypeDeclarationNode>()
        val functions = it.filterIsInstance<FunctionNode>()
        ModuleNode(name, imports, types, typeDeclarations, functions, location)
    }


fun KSharpLexerIterator.consumeModuleNodes(): List<NodeData> =
    consumeModuleNodesLogic().build {
        it.cast<List<NodeData>>()
    }.map {
        it.value
    }.valueOrNull ?: listOf()
