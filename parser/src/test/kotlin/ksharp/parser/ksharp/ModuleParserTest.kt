package ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import ksharp.nodes.ImportNode
import ksharp.nodes.ModuleNode
import ksharp.test.shouldBeRight
import org.ksharp.common.Location

class ModuleParserTest : StringSpec({
    "Parse a module with imports" {
        """
            import ksharp.text as text
            import ksharp.math as math
        """.trimIndent()
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File", listOf(
                        ImportNode("ksharp.text", "text", Location.NoProvided),
                        ImportNode("ksharp.math", "math", Location.NoProvided)
                    ), Location.NoProvided
                )
            )
    }
    "Parse a module without newline at the end" {
        """
            import ksharp.text as text
            import ksharp.math as math""".trimIndent()
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File", listOf(
                        ImportNode("ksharp.text", "text", Location.NoProvided),
                        ImportNode("ksharp.math", "math", Location.NoProvided)
                    ), Location.NoProvided
                )
            )
    }
    "Parse a module with just one import" {
        "import ksharp.text as text"
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File", listOf(
                        ImportNode("ksharp.text", "text", Location.NoProvided)
                    ), Location.NoProvided
                )
            )
    }
})