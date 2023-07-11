package org.ksharp.parser.ksharp

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.nodes.ImportNode
import org.ksharp.nodes.ImportNodeLocations
import org.ksharp.parser.*

private data class ModuleName(
    val name: String,
    val start: Location,
    val end: Location
)

/**
 * [module name grammar](https://docs.ksharp.org/rfc/syntax#modulename)
 */
private fun KSharpLexerIterator.consumeModuleName() =
    disableCollapseDotOperatorRule { l ->
        l.consumeLowerCaseWord()
            .thenLoop {
                it.consumeDot()
                    .thenLowerCaseWord()
                    .build { pair ->
                        pair.joinToString("") { t ->
                            t.cast<LexerValue>().text
                        } to pair.last()
                    }
            }.build {
                val first = it.first().cast<Token>()
                if (it.size == 1) {
                    ModuleName(
                        first.text,
                        first.location,
                        first.location
                    )
                } else {
                    ModuleName(
                        it.joinToString("") { t ->
                            if (t is LexerValue) t.text
                            else t.cast<Pair<String, Token>>().first
                        },
                        first.location,
                        it.last().cast<Pair<String, Token>>().second.location
                    )
                }
            }
    }

fun KSharpLexerIterator.consumeImport(): KSharpParserResult =
    consumeKeyword("import", false)
        .addRelativeIndentationOffset(1, OffsetType.Normal)
        .consume {
            it.consumeModuleName()
        }.thenKeyword("as", false)
        .thenLowerCaseWord()
        .build {
            val importKeyword = it.first().cast<Token>()
            val moduleName = it[1].cast<ModuleName>()
            val asKeyword = it[2].cast<Token>()
            val key = it.last().cast<Token>()
            ImportNode(
                moduleName.name, key.text, importKeyword.location,
                if (state.value.emitLocations) {
                    ImportNodeLocations(
                        importKeyword.location,
                        moduleName.start,
                        moduleName.end,
                        asKeyword.location,
                        key.location
                    )
                } else {
                    ImportNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided
                    )
                }
            )
        }
