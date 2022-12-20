package org.ksharp.parser.ksharp

import org.ksharp.common.cast
import org.ksharp.nodes.ImportNode
import org.ksharp.parser.*

/**
 * [module name grammar](https://docs.ksharp.org/rfc/syntax#modulename)
 */
fun <L : LexerValue> Iterator<L>.consumeModuleName() =
    consumeLowerCaseWord()
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

fun <L : LexerValue> Iterator<L>.consumeImport() =
    consumeKeyword("import")
        .consume {
            it.consumeModuleName()
        }.thenKeyword("as")
        .thenLowerCaseWord()
        .then(KSharpTokenType.EndExpression)
        .build {
            val moduleName = it[1] as String
            val key = it[it.size - 2].cast<LexerValue>().text
            ImportNode(moduleName, key, it.first().cast<LexerValue>().location)
        }