package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset
import org.ksharp.nodes.*
import org.ksharp.test.shouldBeRight
import java.nio.file.Paths

class ModuleParserTest : StringSpec({
    val expectedModule = ModuleNode(
        "File", mapOf(
            "text" to ImportNode("ksharp.text", "text", Location.NoProvided),
            "math" to ImportNode("ksharp.math", "math", Location.NoProvided)
        ), mapOf(), mapOf(), mapOf(), Location.NoProvided
    )
    val expectedModuleWithLocations: (String) -> ModuleNode = {
        ModuleNode(
            it, mapOf(
                "text" to ImportNode(
                    "ksharp.text",
                    "text",
                    Location(context = it, position = Line(value = 1) to Offset(value = 0))
                ),
                "math" to ImportNode(
                    "ksharp.math",
                    "math",
                    Location(context = it, position = Line(value = 2) to Offset(value = 0))
                )
            ), mapOf(), mapOf(), mapOf(), Location(context = it, position = Line(value = 1) to Offset(value = 0))
        )
    }
    "Parse a module with imports" {
        """
            import ksharp.text as text
            import ksharp.math as math
        """.trimIndent()
            .parseModule("File", false)
            .shouldBeRight(expectedModule)
    }
    "Parse a module without newline at the end with locations" {
        """
            import ksharp.text as text
            import ksharp.math as math""".trimIndent()
            .parseModule("File", true)
            .shouldBeRight(expectedModuleWithLocations("File"))
    }
    "Parse a file module without newline at the end" {
        javaClass.getResource("/parseTestResource.txt")!!
            .let { Paths.get(it.toURI()).toFile() }
            .parseModule(false)
            .shouldBeRight(expectedModule.copy("parseTestResource.txt"))
    }
    "Parse a file module without newline at the end with locations" {
        javaClass.getResource("/parseTestResource.txt")!!
            .let { Paths.get(it.toURI()).toFile() }
            .parseModule(true)
            .shouldBeRight(expectedModuleWithLocations("parseTestResource.txt"))
    }
    "Parse a path module without newline at the end" {
        javaClass.getResource("/parseTestResource.txt")!!
            .let { Paths.get(it.toURI()) }
            .parseModule(false)
            .shouldBeRight(expectedModule.copy("parseTestResource.txt"))
    }
    "Parse a path module without newline at the end with locations" {
        javaClass.getResource("/parseTestResource.txt")!!
            .let { Paths.get(it.toURI()) }
            .parseModule(true)
            .shouldBeRight(expectedModuleWithLocations("parseTestResource.txt"))
    }
    "Parse a module with just one import" {
        "import ksharp.text as text"
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File", mapOf(
                        "text" to ImportNode("ksharp.text", "text", Location.NoProvided)
                    ), mapOf(), mapOf(), mapOf(), Location.NoProvided
                )
            )
    }
    "Parse a module with function type declaration" {
        """
            sum :: Int -> Int -> Int
        """.trimIndent()
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File", mapOf(), mapOf(), mapOf(
                        "sum" to TypeDeclarationNode(
                            "sum",
                            FunctionTypeNode(
                                listOf(
                                    ConcreteTypeNode("Int", Location.NoProvided),
                                    ConcreteTypeNode("Int", Location.NoProvided),
                                    ConcreteTypeNode("Int", Location.NoProvided)
                                ),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        )
                    ), mapOf(), Location.NoProvided
                )
            )
    }
    "Parse a module with types" {
        """
            type Age = Int
        """.trimIndent()
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File", mapOf(), mapOf(
                        "Age" to TypeNode(
                            false,
                            "Age",
                            listOf(),
                            ConcreteTypeNode("Int", Location.NoProvided),
                            Location.NoProvided
                        )
                    ), mapOf(), mapOf(), Location.NoProvided
                )
            )
    }
    "Parse a module with imports, types and type declarations" {
        """
            import ksharp.text as text
            
            type Age = Int
            
            sum :: Int -> Int -> Int
        """.trimIndent()
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File", mapOf(
                        "text" to ImportNode("ksharp.text", "text", Location.NoProvided),
                    ), mapOf(
                        "Age" to TypeNode(
                            false,
                            "Age",
                            listOf(),
                            ConcreteTypeNode("Int", Location.NoProvided),
                            Location.NoProvided
                        )
                    ), mapOf(
                        "sum" to TypeDeclarationNode(
                            "sum",
                            FunctionTypeNode(
                                listOf(
                                    ConcreteTypeNode("Int", Location.NoProvided),
                                    ConcreteTypeNode("Int", Location.NoProvided),
                                    ConcreteTypeNode("Int", Location.NoProvided)
                                ),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        )
                    ), mapOf(), Location.NoProvided
                )
            )
    }
})