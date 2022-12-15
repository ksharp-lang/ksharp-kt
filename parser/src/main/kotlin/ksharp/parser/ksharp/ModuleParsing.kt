package ksharp.parser.ksharp

import ksharp.parser.LexerToken
import ksharp.parser.build
import ksharp.parser.thenLoop

/**
 * [module name grammar](https://docs.ksharp.org/rfc/syntax#modulename)
 */
fun Iterator<LexerToken>.moduleName() =
    consumeLowerCaseWord()
        .thenLoop {
            consumeDot()
                .thenLowerCaseWord()
                .build {
                    it.joinToString("") { t ->
                        t as LexerToken
                        t.text
                    }
                }
        }.build {
            it.joinToString("") { t ->
                t as LexerToken
                t.text
            }
        }