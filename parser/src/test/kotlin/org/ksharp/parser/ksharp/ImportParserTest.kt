package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.nodes.ImportNode
import org.ksharp.nodes.ImportNodeLocations
import org.ksharp.parser.LexerToken
import org.ksharp.parser.TextToken
import org.ksharp.parser.enableLookAhead
import org.ksharp.test.shouldBeRight

class ImportParserTest : StringSpec({
    "Parse import" {
        "import ksharp.math as math"
            .kSharpLexer()
            .ensureNewLineAtEnd()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .enableLookAhead()
            .collapseKSharpTokens()
            .discardBlocksOrNewLineTokens()
            .consumeBlock { it.consumeImport() }
            .map { it.value }
            .shouldBeRight(
                ImportNode(
                    "ksharp.math",
                    "math",
                    Location.NoProvided,
                    ImportNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided
                    )
                )
            )
    }
    "Parse import 2" {
        "import ksharp as math"
            .kSharpLexer()
            .ensureNewLineAtEnd()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .enableLookAhead()
            .collapseKSharpTokens()
            .discardBlocksOrNewLineTokens()
            .consumeBlock { it.consumeImport() }
            .map { it.value }
            .shouldBeRight(
                ImportNode(
                    "ksharp",
                    "math",
                    Location.NoProvided,
                    ImportNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided
                    )
                )
            )
    }
})
