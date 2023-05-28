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
        "File", listOf(
            ImportNode("ksharp.text", "text", Location.NoProvided),
            ImportNode("ksharp.math", "math", Location.NoProvided)
        ), listOf(), listOf(), listOf(), Location.NoProvided
    )
    val expectedModuleWithLocations: (String) -> ModuleNode = {
        ModuleNode(
            it, listOf(
                ImportNode(
                    "ksharp.text",
                    "text",
                    Location(context = it, position = Line(value = 1) to Offset(value = 0))
                ),
                ImportNode(
                    "ksharp.math",
                    "math",
                    Location(context = it, position = Line(value = 2) to Offset(value = 0))
                )
            ), listOf(), listOf(), listOf(), Location(context = it, position = Line(value = 1) to Offset(value = 0))
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
                    "File", listOf(
                        ImportNode("ksharp.text", "text", Location.NoProvided)
                    ), listOf(), listOf(), listOf(), Location.NoProvided
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
                    "File", listOf(), listOf(), listOf(
                        TypeDeclarationNode(
                            null,
                            "sum",
                            listOf(),
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
                    ), listOf(), Location.NoProvided
                )
            )
    }
    "Parse a module with function type declaration and annotation" {
        """
            @native(lang="java")
            sum :: Int -> Int -> Int
        """.trimIndent()
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File", listOf(), listOf(), listOf(
                        TypeDeclarationNode(
                            listOf(AnnotationNode("native", mapOf("lang" to "java"), Location.NoProvided)),
                            "sum",
                            listOf(),
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
                    ), listOf(), Location.NoProvided
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
                    "File", listOf(), listOf(
                        TypeNode(
                            false,
                            null,
                            "Age",
                            listOf(),
                            ConcreteTypeNode("Int", Location.NoProvided),
                            Location.NoProvided
                        )
                    ), listOf(), listOf(), Location.NoProvided
                )
            )
    }
    "Parse a module with types and annotation" {
        """
            @native(flag=True)
            type Age = Int
            type Age2 = Int
        """.trimIndent()
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File", listOf(), listOf(
                        TypeNode(
                            false,
                            listOf(AnnotationNode("native", mapOf("flag" to true), Location.NoProvided)),
                            "Age",
                            listOf(),
                            ConcreteTypeNode("Int", Location.NoProvided),
                            Location.NoProvided
                        ),
                        TypeNode(
                            false,
                            null,
                            "Age2",
                            listOf(),
                            ConcreteTypeNode("Int", Location.NoProvided),
                            Location.NoProvided
                        )
                    ), listOf(), listOf(), Location.NoProvided
                )
            )
    }
    "Parse a module with traits" {
        """
         type Age = Int
         trait Num a  =
            sum :: Int -> Int -> Int
        """.trimIndent()
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File", listOf(), listOf(
                        TypeNode(
                            false,
                            null,
                            "Age",
                            listOf(),
                            ConcreteTypeNode("Int", Location.NoProvided),
                            Location.NoProvided
                        ),
                        TraitNode(
                            false,
                            null,
                            "Num",
                            listOf("a"),
                            TraitFunctionsNode(
                                listOf(
                                    TraitFunctionNode(
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
                                )
                            ),
                            Location.NoProvided
                        )
                    ), listOf(), listOf(), Location.NoProvided
                )
            )
    }
    "Parse a module with traits and annotations" {
        """
         type Age = Int
         @native(name="java")
         trait Num a  =
            sum :: Int -> Int -> Int
        """.trimIndent()
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File", listOf(), listOf(
                        TypeNode(
                            false,
                            null,
                            "Age",
                            listOf(),
                            ConcreteTypeNode("Int", Location.NoProvided),
                            Location.NoProvided
                        ),
                        TraitNode(
                            false,
                            listOf(AnnotationNode("native", mapOf("name" to "java"), Location.NoProvided)),
                            "Num",
                            listOf("a"),
                            TraitFunctionsNode(
                                listOf(
                                    TraitFunctionNode(
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
                                )
                            ),
                            Location.NoProvided
                        )
                    ), listOf(), listOf(), Location.NoProvided
                )
            )
    }
    "Parse a module with function" {
        """
            sum a b = a + b
        """.trimIndent()
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File", listOf(), listOf(), listOf(), listOf(
                        FunctionNode(
                            false,
                            null,
                            "sum",
                            listOf("a", "b"),
                            OperatorNode(
                                "+",
                                FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                                FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        )
                    ), Location.NoProvided
                )
            )
    }
    "Parse a module with function and annotation" {
        """
            @native(flag=False)
            sum a b = a + b
        """.trimIndent()
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File", listOf(), listOf(), listOf(), listOf(
                        FunctionNode(
                            false,
                            listOf(AnnotationNode("native", mapOf("flag" to false), Location.NoProvided)),
                            "sum",
                            listOf("a", "b"),
                            OperatorNode(
                                "+",
                                FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                                FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        )
                    ), Location.NoProvided
                )
            )
    }
    "Parse a module with imports, types and type declarations and functions" {
        """
            import ksharp.text as text
            
            type Age = Int
            
            sum :: Int -> Int -> Int
            
            pub sum a b = a + b
        """.trimIndent()
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File", listOf(ImportNode("ksharp.text", "text", Location.NoProvided)),
                    listOf(
                        TypeNode(
                            false,
                            null,
                            "Age",
                            listOf(),
                            ConcreteTypeNode("Int", Location.NoProvided),
                            Location.NoProvided
                        )
                    ), listOf(
                        TypeDeclarationNode(
                            null,
                            "sum",
                            listOf(),
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
                    ), listOf(
                        FunctionNode(
                            true,
                            null,
                            "sum",
                            listOf("a", "b"),
                            OperatorNode(
                                "+",
                                FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                                FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        )
                    ), Location.NoProvided
                )
            )
    }
})
