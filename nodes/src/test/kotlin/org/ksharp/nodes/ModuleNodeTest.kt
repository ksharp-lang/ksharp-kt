package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

private val testLocation = Location.NoProvided

class ModuleNodeTest : StringSpec({
    "Test Node Interface over ModuleNode" {
        ModuleNode(
            name = "ksharp.math",
            listOf(
                ImportNode(
                    "ksharp.num", "n", testLocation,
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
                    false, null, "Age", listOf(), ConcreteTypeNode("Int", testLocation), testLocation,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            ),
            emptyList(),
            emptyList(),
            listOf(
                TypeDeclarationNode(
                    null, "sum", listOf(), ConcreteTypeNode("Int", testLocation), testLocation,
                    TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                )
            ),
            listOf(),
            listOf(),
            testLocation
        ).node.apply {
            cast<ModuleNode>().apply {
                name.shouldBe("ksharp.math")
                imports.shouldBe(
                    listOf(
                        ImportNode(
                            "ksharp.num", "n", testLocation, ImportNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided
                            )
                        )
                    )
                )
                typeDeclarations.shouldBe(
                    listOf(
                        TypeDeclarationNode(
                            null,
                            "sum",
                            listOf(),
                            ConcreteTypeNode("Int", testLocation),
                            testLocation,
                            TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                        )
                    )
                )
                types.shouldBe(
                    listOf(
                        TypeNode(
                            false,
                            null,
                            "Age",
                            listOf(),
                            ConcreteTypeNode("Int", testLocation),
                            testLocation,
                            TypeNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                listOf(),
                                Location.NoProvided
                            )
                        )
                    )
                )
                functions.shouldBeEmpty()
                errors.shouldBeEmpty()
                location.shouldBe(testLocation)
                locations.shouldBe(NoLocationsDefined)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this, testLocation, ImportNode(
                            "ksharp.num", "n", testLocation, ImportNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided
                            )
                        )
                    ),
                    Node(
                        this,
                        testLocation,
                        TypeNode(
                            false,
                            null,
                            "Age",
                            listOf(),
                            ConcreteTypeNode("Int", testLocation),
                            testLocation,
                            TypeNodeLocations(
                                Location.NoProvided,
                                Location.NoProvided,
                                Location.NoProvided,
                                listOf(),
                                Location.NoProvided
                            )
                        )
                    ),
                    Node(
                        this,
                        testLocation,
                        TypeDeclarationNode(
                            null,
                            "sum",
                            listOf(),
                            ConcreteTypeNode("Int", testLocation),
                            testLocation,
                            TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                        )
                    )
                )
            )
        }
    }
})
