package org.ksharp.parser.ksharp

import org.ksharp.common.cast
import org.ksharp.nodes.ImportNode
import org.ksharp.parser.*

/**
 * [module name grammar](https://docs.ksharp.org/rfc/syntax#modulename)
 */
internal fun KSharpLexerIterator.consumeModuleName() =
    disableCollapseDotOperatorRule { l ->
        l.consumeLowerCaseWord()
            .thenLoop {
                it.consumeDot()
                    .thenLowerCaseWord()
                    .build { pair ->
                        pair.joinToString("") { t ->
                            t as LexerValue
                            t.text
                        }
                    }
            }.build {
                it.joinToString("") { t ->
                    if (t is LexerValue) t.text
                    else t.toString()
                }
            }
    }

fun KSharpLexerIterator.consumeImport(): KSharpParserResult =
    consumeKeyword("import")
        .consume {
            it.consumeModuleName()
        }.thenKeyword("as", true)
        .thenLowerCaseWord()
        .build {
            val moduleName = it[1] as String
            val key = it.last().cast<LexerValue>().text
            ImportNode(moduleName, key, it.first().cast<LexerValue>().location)
        }