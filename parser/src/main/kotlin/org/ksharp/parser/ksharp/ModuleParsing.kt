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

private fun isTopLevelNewLine(token: Token): Boolean =
    token.type == BaseTokenType.NewLine && token.text.indentLength() == 0

private fun KSharpConsumeResult.consumeInvalidTokens(error: Error): KSharpParserResult =
    thenLoop { t ->
        t.consume({ !isTopLevelNewLine(it) })
            .build { it.first() }
    }.build {
        InvalidNode(
            it.asSequence()
                .filter { t -> t.cast<Token>().type != BaseTokenType.NewLine }
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
    }.flatMap {
        if (it.value.tokens.isEmpty()) Either.Left(
            ParserError(
                it.value.error,
                listBuilder(),
                false,
                it.remainTokens
            )
        )
        else Either.Right(ParserValue(it.value, it.remainTokens))
    }

private fun KSharpParserResult.consumeInvalidTokens(state: KSharpLexerState): KSharpParserResult =
    mapLeft { e ->
        state.lastError.set(e.error)
        e
    }.or { l -> l.collect().consumeInvalidTokens(state.lastError.get()!!) }

private fun KSharpLexerIterator.thenTopLevelSymbol(
    block: (KSharpLexerIterator) -> KSharpParserResult
): KSharpConsumeResult =
    collect()
        .thenLoop { l ->
            block(l)
        }

private fun KSharpLexerIterator.consumeModuleNodesLogic(): KSharpConsumeResult =
    thenTopLevelSymbol { then ->
        then.consumeImport()
            .or { l -> l.consumeFunctionTypeDeclaration() }
            .or { l -> l.consumeTypeDeclaration() }
            .or { l -> l.consumeAnnotation() }
            .or { l -> l.consumeFunction() }
            .consumeInvalidTokens(state.value)
            .resume()
            .then(
                ::isTopLevelNewLine,
                { createExpectedTokenError("Newline", it) },
                true
            ).build {
                it.first().cast<NodeData>()
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
