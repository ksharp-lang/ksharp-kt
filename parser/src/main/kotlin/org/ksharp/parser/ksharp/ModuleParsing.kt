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
                        .or { l -> l.consumeAnnotation() }
                        .or { l -> l.consumeFunction() }
                }.asLookAHeadResult()
            }.mapLeft {
                state.value.lastError.set(it.error)
                it
            }.orCollect { l -> l.consumeInvalidTokens(state.value.lastError.get()!!) }
        }

fun List<NodeData>.toModuleNode(name: String): ModuleNode {
    val location = firstOrNull()?.cast<NodeData>()?.location ?: Location.NoProvided
    val imports = filterIsInstance<ImportNode>()
    val types = filter { n -> n is TypeNode || n is TraitNode }
    val typeDeclarations = filterIsInstance<TypeDeclarationNode>()
    val functions = filterIsInstance<FunctionNode>()
    val errors = asSequence().filterIsInstance<InvalidNode>().map {
        it.error
    }.toList()
    return ModuleNode(name, imports, types, typeDeclarations, functions, errors, location)
}

fun KSharpLexerIterator.consumeModule(name: String): ParserResult<ModuleNode, KSharpLexerState> =
    consumeModuleNodesLogic().build {
        it.cast<List<NodeData>>().toModuleNode(name)
    }


fun KSharpLexerIterator.consumeModuleNodes(): List<NodeData> =
    consumeModuleNodesLogic().build {
        it.cast<List<NodeData>>()
    }.map { it.value }.valueOrNull ?: listOf()
