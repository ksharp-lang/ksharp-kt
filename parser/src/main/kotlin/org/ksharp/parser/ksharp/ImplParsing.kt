package org.ksharp.parser.ksharp

import org.ksharp.common.cast
import org.ksharp.nodes.FunctionNode
import org.ksharp.nodes.ImplNode
import org.ksharp.nodes.ImplNodeLocations
import org.ksharp.parser.*

private fun KSharpConsumeResult.thenImplFunction(): KSharpParserResult =
    consume {
        it.consumeAnnotation()
            .or { l -> l.consumeFunction() }
    }.build {
        it.first().cast()
    }

fun KSharpLexerIterator.consumeImpl(): KSharpParserResult =
    consumeKeyword("impl", false)
        .map {
            //remove all annotations, impl doesn't support annotations
            state.value.annotations.build()
            it
        }
        .thenUpperCaseWord()
        .thenKeyword("for", false)
        .thenUpperCaseWord()
        .thenAssignOperator()
        .thenRepeatingIndentation(true) { l ->
            l.thenImplFunction()
        }.build {
            //remove all impl function annotations
            state.value.annotations.build()
            val implKeywordToken = it[0].cast<Token>()
            val traitNameToken = it[1].cast<Token>()
            val forKeywordToken = it[2].cast<Token>()
            val typeToken = it[3].cast<Token>()
            val assignOperatorToken = it[4].cast<Token>()
            val functions = it.subList(5, it.size - 1).cast<List<FunctionNode>>()
            ImplNode(
                traitNameToken.text,
                typeToken.text,
                functions,
                implKeywordToken.location,
                ImplNodeLocations(
                    traitNameToken.location,
                    forKeywordToken.location,
                    typeToken.location,
                    assignOperatorToken.location
                )
            )
        }
