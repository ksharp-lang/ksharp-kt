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
            ImportNode(
                "ksharp.text", "text", Location.NoProvided,
                ImportNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided
                )
            ),
            ImportNode(
                "ksharp.math", "math", Location.NoProvided, ImportNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided
                )
            )
        ), listOf(), listOf(), listOf(), listOf(), Location.NoProvided
    )
    val expectedModuleWithLocations: (String) -> ModuleNode = {
        ModuleNode(
            it,
            listOf(
                ImportNode(
                    "ksharp.text",
                    "text",
                    Location(Line(value = 1) to Offset(value = 0), Line(value = 1) to Offset(value = 6)),
                    ImportNodeLocations(
                        importLocation = Location(
                            start = (Line(value = 1) to Offset(value = 0)),
                            end = (Line(value = 1) to Offset(value = 6))
                        ),
                        moduleNameBegin = Location(
                            start = (Line(value = 1) to Offset(value = 7)),
                            end = (Line(value = 1) to Offset(value = 13))
                        ),
                        moduleNameEnd = Location(
                            start = (Line(value = 1) to Offset(value = 14)),
                            end = (Line(value = 1) to Offset(value = 18))
                        ),
                        asLocation = Location(
                            start = (Line(value = 1) to Offset(value = 19)),
                            end = (Line(value = 1) to Offset(value = 21))
                        ),
                        keyLocation = Location(
                            start = (Line(value = 1) to Offset(value = 22)),
                            end = (Line(value = 1) to Offset(value = 26))
                        )
                    )
                ),
                ImportNode(
                    "ksharp.math",
                    "math",
                    Location(Line(value = 2) to Offset(value = 0), Line(value = 2) to Offset(value = 6)),
                    ImportNodeLocations(
                        importLocation = Location(
                            start = (Line(value = 2) to Offset(value = 0)),
                            end = (Line(value = 2) to Offset(value = 6))
                        ),
                        moduleNameBegin = Location(
                            start = (Line(value = 2) to Offset(value = 7)),
                            end = (Line(value = 2) to Offset(value = 13))
                        ),
                        moduleNameEnd = Location(
                            start = (Line(value = 2) to Offset(value = 14)),
                            end = (Line(value = 2) to Offset(value = 18))
                        ),
                        asLocation = Location(
                            start = (Line(value = 2) to Offset(value = 19)),
                            end = (Line(value = 2) to Offset(value = 21))
                        ),
                        keyLocation = Location(
                            start = (Line(value = 2) to Offset(value = 22)),
                            end = (Line(value = 2) to Offset(value = 26))
                        )
                    )
                )
            ),
            listOf(),
            listOf(),
            listOf(),
            listOf(),
            Location(Line(value = 1) to Offset(value = 0), Line(value = 1) to Offset(value = 6))
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
                        ImportNode(
                            "ksharp.text", "text", Location.NoProvided,
                            ImportNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided
                            )
                        )
                    ), listOf(), listOf(), listOf(), listOf(), Location.NoProvided
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
                                Location.NoProvided, FunctionTypeNodeLocations(listOf())
                            ),
                            Location.NoProvided,
                            TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                        )
                    ), listOf(), listOf(), Location.NoProvided
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
                            listOf(
                                AnnotationNode(
                                    "native", mapOf("lang" to "java"), Location.NoProvided, AnnotationNodeLocations(
                                        Location.NoProvided, Location.NoProvided, listOf()
                                    )
                                )
                            ),
                            "sum",
                            listOf(),
                            FunctionTypeNode(
                                listOf(
                                    ConcreteTypeNode("Int", Location.NoProvided),
                                    ConcreteTypeNode("Int", Location.NoProvided),
                                    ConcreteTypeNode("Int", Location.NoProvided)
                                ),
                                Location.NoProvided, FunctionTypeNodeLocations(listOf())
                            ),
                            Location.NoProvided,
                            TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                        )
                    ), listOf(), listOf(), Location.NoProvided
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
                            Location.NoProvided,
                            TypeNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                listOf(),
                                Location.NoProvided
                            )
                        )
                    ), listOf(), listOf(), listOf(), Location.NoProvided
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
                            listOf(
                                AnnotationNode(
                                    "native", mapOf("flag" to true), Location.NoProvided,
                                    AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                                )
                            ),
                            "Age",
                            listOf(),
                            ConcreteTypeNode("Int", Location.NoProvided),
                            Location.NoProvided,
                            TypeNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                listOf(),
                                Location.NoProvided
                            )
                        ),
                        TypeNode(
                            false,
                            null,
                            "Age2",
                            listOf(),
                            ConcreteTypeNode("Int", Location.NoProvided),
                            Location.NoProvided,
                            TypeNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                listOf(),
                                Location.NoProvided
                            )
                        )
                    ), listOf(), listOf(), listOf(), Location.NoProvided
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
                            Location.NoProvided,
                            TypeNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                listOf(),
                                Location.NoProvided
                            )
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
                                            Location.NoProvided, FunctionTypeNodeLocations(listOf())
                                        ),
                                        Location.NoProvided,
                                        TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                                    )
                                )
                            ),
                            Location.NoProvided,
                            TraitNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                listOf(),
                                Location.NoProvided
                            )
                        )
                    ), listOf(), listOf(), listOf(), Location.NoProvided
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
                            Location.NoProvided,
                            TypeNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                listOf(),
                                Location.NoProvided
                            )
                        ),
                        TraitNode(
                            false,
                            listOf(
                                AnnotationNode(
                                    "native", mapOf("name" to "java"), Location.NoProvided,
                                    AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                                )
                            ),
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
                                            Location.NoProvided, FunctionTypeNodeLocations(listOf())
                                        ),
                                        Location.NoProvided,
                                        TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                                    )
                                )
                            ),
                            Location.NoProvided,
                            TraitNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                listOf(),
                                Location.NoProvided
                            )
                        )
                    ), listOf(), listOf(), listOf(), Location.NoProvided
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
                            false,
                            null,
                            "sum",
                            listOf("a", "b"),
                            OperatorNode(
                                "+",
                                FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                                FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                                Location.NoProvided,

                                ),
                            Location.NoProvided,
                            FunctionNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                listOf(),
                                Location.NoProvided
                            )
                        )
                    ), listOf(), Location.NoProvided
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
                            false,
                            listOf(
                                AnnotationNode(
                                    "native", mapOf("flag" to false), Location.NoProvided,
                                    AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                                )
                            ),
                            "sum",
                            listOf("a", "b"),
                            OperatorNode(
                                "+",
                                FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                                FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                                Location.NoProvided,

                                ),
                            Location.NoProvided,
                            FunctionNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                listOf(),
                                Location.NoProvided
                            )
                        )
                    ), listOf(), Location.NoProvided
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
                    "File", listOf(
                        ImportNode(
                            "ksharp.text", "text", Location.NoProvided,
                            ImportNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided
                            )
                        )
                    ),
                    listOf(
                        TypeNode(
                            false,
                            null,
                            "Age",
                            listOf(),
                            ConcreteTypeNode("Int", Location.NoProvided),
                            Location.NoProvided,
                            TypeNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                listOf(),
                                Location.NoProvided
                            )
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
                                Location.NoProvided,
                                FunctionTypeNodeLocations(listOf())
                            ),
                            Location.NoProvided,
                            TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                        )
                    ), listOf(
                        FunctionNode(
                            false,
                            true,
                            null,
                            "sum",
                            listOf("a", "b"),
                            OperatorNode(
                                "+",
                                FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                                FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                                Location.NoProvided,
                            ),
                            Location.NoProvided, FunctionNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                listOf(),
                                Location.NoProvided
                            )
                        )
                    ), listOf(), Location.NoProvided
                )
            )
    }
    "Parse a module with more than one function" {
        """
            |sum a b = a + b
            |sum2 a b = a + b
        """.trimMargin("|")
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File",
                    listOf(),
                    listOf(),
                    listOf(),
                    listOf(
                        FunctionNode(
                            native = false,
                            pub = false,
                            annotations = null,
                            name = "sum",
                            parameters = listOf("a", "b"),
                            expression = OperatorNode(
                                operator = "+",
                                left = FunctionCallNode(
                                    name = "a",
                                    type = FunctionType.Function,
                                    arguments = listOf(),
                                    location = Location.NoProvided
                                ),
                                right = FunctionCallNode(
                                    name = "b",
                                    type = FunctionType.Function,
                                    arguments = listOf(),
                                    location = Location.NoProvided
                                ),
                                location = Location.NoProvided
                            ),
                            location = Location.NoProvided,
                            locations = FunctionNodeLocations(
                                nativeLocation = Location.NoProvided,
                                pubLocation = Location.NoProvided,
                                name = Location.NoProvided,
                                parameters = listOf(),
                                assignOperator = Location.NoProvided
                            )
                        ), FunctionNode(
                            native = false,
                            pub = false,
                            annotations = null,
                            name = "sum2",
                            parameters = listOf("a", "b"),
                            expression = OperatorNode(
                                operator = "+",
                                left = FunctionCallNode(
                                    name = "a",
                                    type = FunctionType.Function,
                                    arguments = listOf(),
                                    location = Location.NoProvided
                                ),
                                right = FunctionCallNode(
                                    name = "b",
                                    type = FunctionType.Function,
                                    arguments = listOf(),
                                    location = Location.NoProvided
                                ),
                                location = Location.NoProvided
                            ),
                            location = Location.NoProvided,
                            locations = FunctionNodeLocations(
                                nativeLocation = Location.NoProvided,
                                pubLocation = Location.NoProvided,
                                name = Location.NoProvided,
                                parameters = listOf(),
                                assignOperator = Location.NoProvided
                            )
                        )
                    ),
                    listOf(),
                    Location.NoProvided
                )
            )
    }
    "Parse a module with a function with annotation" {
        """
            @native(lang=["java", "c#"])
            var = 10
        """.trimIndent()
            .parseModule("File", false)
            .shouldBeRight(
                ModuleNode(
                    "File",
                    listOf(),
                    listOf(),
                    listOf(),
                    listOf(
                        FunctionNode(
                            native = false,
                            pub = false,
                            annotations = listOf(
                                AnnotationNode(
                                    name = "native",
                                    attrs = mapOf("lang" to listOf("java", "c#")),
                                    location = Location.NoProvided,
                                    locations = AnnotationNodeLocations(
                                        altLocation = Location.NoProvided,
                                        name = Location.NoProvided,
                                        attrs = listOf()
                                    )
                                )
                            ),
                            name = "var",
                            parameters = listOf(),
                            expression = LiteralValueNode(
                                value = "10",
                                type = LiteralValueType.Integer,
                                location = Location.NoProvided
                            ),
                            location = Location.NoProvided,
                            locations = FunctionNodeLocations(
                                nativeLocation = Location.NoProvided,
                                pubLocation = Location.NoProvided,
                                name = Location.NoProvided,
                                parameters = listOf(),
                                assignOperator = Location.NoProvided
                            )
                        )
                    ),
                    listOf(),
                    Location.NoProvided
                )
            )
    }
    "Parse two types with carrie return and line feed" {
        "type Unit = KernelUnit\r\ntype Char = KernelChar"
            .parseModule("File", false)
            .map {
                it.types.onEach(::println)
            }
            .shouldBeRight(
                ModuleNode(
                    "File",
                    listOf(),
                    listOf(
                        TypeNode(
                            internal = false,
                            annotations = null,
                            name = "Unit",
                            params = listOf(),
                            expr = ConcreteTypeNode(name = "KernelUnit", location = Location.NoProvided),
                            location = Location.NoProvided,
                            locations = TypeNodeLocations(
                                internalLocation = Location.NoProvided,
                                typeLocation = Location.NoProvided,
                                name = Location.NoProvided,
                                params = listOf(),
                                assignOperatorLocation = Location.NoProvided
                            )
                        ),
                        TypeNode(
                            internal = false,
                            annotations = null,
                            name = "Char",
                            params = listOf(),
                            expr = ConcreteTypeNode(name = "KernelChar", location = Location.NoProvided),
                            location = Location.NoProvided,
                            locations = TypeNodeLocations(
                                internalLocation = Location.NoProvided,
                                typeLocation = Location.NoProvided,
                                name = Location.NoProvided,
                                params = listOf(),
                                assignOperatorLocation = Location.NoProvided
                            )
                        )
                    ),
                    listOf(),
                    listOf(),
                    listOf(),
                    Location.NoProvided
                )
            )
    }
})
