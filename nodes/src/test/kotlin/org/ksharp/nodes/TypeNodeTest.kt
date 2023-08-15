package org.ksharp.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location

private val testLocation = Location.NoProvided

class TypeNodeTest : StringSpec({
    "Test Node Interface over TraitFunctionNode" {
        TraitFunctionNode(
            "sum",
            InvalidSetTypeNode(testLocation),
            testLocation,
            TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
        ).node.apply {
            cast<TraitFunctionNode>().apply {
                name.shouldBe("sum")
                type.shouldBe(InvalidSetTypeNode(testLocation))
                location.shouldBe(testLocation)
                locations.shouldBe(TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided))
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(Node(this, testLocation, InvalidSetTypeNode(testLocation)))
            )
        }
    }
    "Test Node Interface over TraitFunctionsNode" {
        TraitFunctionsNode(
            listOf(
                TraitFunctionNode(
                    "sum",
                    InvalidSetTypeNode(testLocation),
                    testLocation,
                    TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                )
            ), emptyList()
        ).node.apply {
            cast<TraitFunctionsNode>().apply {
                definitions.shouldBe(
                    listOf(
                        TraitFunctionNode(
                            "sum",
                            InvalidSetTypeNode(testLocation),
                            testLocation,
                            TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                        )
                    )
                )
                location.shouldBe(Location.NoProvided)
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this, testLocation, TraitFunctionNode(
                            "sum",
                            InvalidSetTypeNode(testLocation),
                            testLocation,
                            TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                        )
                    )
                )
            )
        }
    }
    "Test Node Interface over TraitNode" {
        TraitNode(
            true,
            emptyList(),
            "Num",
            listOf("a"),
            TraitFunctionsNode(listOf(), emptyList()),
            testLocation,
            TraitNodeLocations(
                Location.NoProvided,
                Location.NoProvided,
                Location.NoProvided,
                listOf(),
                Location.NoProvided
            )
        ).node.apply {
            cast<TraitNode>().apply {
                internal.shouldBeTrue()
                annotations.shouldBeEmpty()
                name.shouldBe("Num")
                params.shouldBe(listOf("a"))
                definition.shouldBe(TraitFunctionsNode(listOf(), emptyList()))
                location.shouldBe(testLocation)
                locations.shouldBe(
                    TraitNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this, testLocation, TraitFunctionsNode(listOf(), emptyList())
                    )
                )
            )
        }
    }
    "Test Node Interface over LabelTypeNode" {
        LabelTypeNode(
            "key",
            ParameterTypeNode("k", testLocation),
            testLocation
        ).node.apply {
            cast<LabelTypeNode>().apply {
                name.shouldBe("key")
                expr.shouldBe(ParameterTypeNode("k", testLocation))
                location.shouldBe(testLocation)
                locations.shouldBe(NoLocationsDefined)
                representation.shouldBe("key: k")
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(
                        this, testLocation, ParameterTypeNode("k", testLocation)
                    )
                )
            )
        }
    }
    "Test Node Interface over ConcreteTypeNode" {
        ConcreteTypeNode(
            "Int",
            testLocation
        ).node.apply {
            cast<ConcreteTypeNode>().apply {
                name.shouldBe("Int")
                location.shouldBe(testLocation)
                representation.shouldBe("Int")
            }
            parent.shouldBeNull()
            children.toList().shouldBeEmpty()
        }
    }
    "Test Node Interface over ParameterTypeNode" {
        ParameterTypeNode(
            "k",
            testLocation
        ).node.apply {
            cast<ParameterTypeNode>().apply {
                name.shouldBe("k")
                location.shouldBe(testLocation)
                representation.shouldBe("k")
            }
            parent.shouldBeNull()
            children.toList().shouldBeEmpty()
        }
    }
    "Test Node Interface over ParametricTypeNode" {
        ParametricTypeNode(
            listOf(ConcreteTypeNode("List", testLocation), ConcreteTypeNode("Int", testLocation)),
            testLocation
        ).node.apply {
            cast<ParametricTypeNode>().apply {
                variables.shouldBe(
                    listOf(
                        ConcreteTypeNode("List", testLocation),
                        ConcreteTypeNode("Int", testLocation)
                    )
                )
                location.shouldBe(testLocation)
                representation.shouldBe("(List Int)")
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(this, testLocation, ConcreteTypeNode("List", testLocation)),
                    Node(this, testLocation, ConcreteTypeNode("Int", testLocation))
                ),
            )
        }
    }
    "Test Node Interface over InvalidSetTypeNode" {
        InvalidSetTypeNode(
            testLocation
        ).node.apply {
            cast<InvalidSetTypeNode>().apply {
                location.shouldBe(testLocation)
            }
            parent.shouldBeNull()
            children.toList().shouldBeEmpty()
        }
    }
    "Test Node Interface over FunctionTypeNode" {
        FunctionTypeNode(
            listOf(ConcreteTypeNode("Int", testLocation), ConcreteTypeNode("Int", testLocation)),
            testLocation,
            FunctionTypeNodeLocations(listOf())
        ).node.apply {
            cast<FunctionTypeNode>().apply {
                params.shouldBe(listOf(ConcreteTypeNode("Int", testLocation), ConcreteTypeNode("Int", testLocation)))
                location.shouldBe(testLocation)
                locations.shouldBe(FunctionTypeNodeLocations(listOf()))
                representation.shouldBe("(Int -> Int)")
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(this, testLocation, ConcreteTypeNode("Int", testLocation)),
                    Node(this, testLocation, ConcreteTypeNode("Int", testLocation))
                )
            )
        }
    }
    "Test Node Interface over TupleTypeNode" {
        TupleTypeNode(
            listOf(ConcreteTypeNode("Int", testLocation), ConcreteTypeNode("Int", testLocation)),
            testLocation,
            TupleTypeNodeLocations(listOf())
        ).node.apply {
            cast<TupleTypeNode>().apply {
                types.shouldBe(listOf(ConcreteTypeNode("Int", testLocation), ConcreteTypeNode("Int", testLocation)))
                location.shouldBe(testLocation)
                locations.shouldBe(TupleTypeNodeLocations(listOf()))
                representation.shouldBe("(Int, Int)")
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(this, testLocation, ConcreteTypeNode("Int", testLocation)),
                    Node(this, testLocation, ConcreteTypeNode("Int", testLocation))
                )
            )
        }
    }
    "Test Node Interface over UnionTypeNode" {
        UnionTypeNode(
            listOf(ConcreteTypeNode("True", testLocation), ConcreteTypeNode("False", testLocation)),
            testLocation,
            UnionTypeNodeLocations(listOf())
        ).node.apply {
            cast<UnionTypeNode>().apply {
                types.shouldBe(listOf(ConcreteTypeNode("True", testLocation), ConcreteTypeNode("False", testLocation)))
                location.shouldBe(testLocation)
                locations.shouldBe(UnionTypeNodeLocations(listOf()))
                representation.shouldBe("(True | False)")
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(this, testLocation, ConcreteTypeNode("True", testLocation)),
                    Node(this, testLocation, ConcreteTypeNode("False", testLocation))
                )
            )
        }
    }
    "Test Node Interface over IntersectionTypeNode" {
        IntersectionTypeNode(
            listOf(ConcreteTypeNode("Int", testLocation), ConcreteTypeNode("String", testLocation)),
            testLocation,
            IntersectionTypeNodeLocations(listOf())
        ).node.apply {
            cast<IntersectionTypeNode>().apply {
                types.shouldBe(listOf(ConcreteTypeNode("Int", testLocation), ConcreteTypeNode("String", testLocation)))
                location.shouldBe(testLocation)
                locations.shouldBe(IntersectionTypeNodeLocations(listOf()))
                representation.shouldBe("(Int & String)")
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(this, testLocation, ConcreteTypeNode("Int", testLocation)),
                    Node(this, testLocation, ConcreteTypeNode("String", testLocation))
                )
            )
        }
    }
    "Test Node Interface over SetElement" {
        SetElement(
            true,
            ConcreteTypeNode("Int", testLocation),
            testLocation
        ).node.apply {
            cast<SetElement>().apply {
                union.shouldBeTrue()
                expression.shouldBe(ConcreteTypeNode("Int", testLocation))
                location.shouldBe(testLocation)
            }
            parent.shouldBeNull()
            children.toList().shouldBeEmpty()
        }
    }
    "Test Node Interface over ConstrainedTypeNode" {
        ConstrainedTypeNode(
            ConcreteTypeNode("Int", testLocation),
            UnitNode(testLocation),
            testLocation,
            ConstrainedTypeNodeLocations(Location.NoProvided)
        ).node.apply {
            cast<ConstrainedTypeNode>().apply {
                type.shouldBe(ConcreteTypeNode("Int", testLocation))
                expression.shouldBe(UnitNode(testLocation))
                location.shouldBe(testLocation)
                locations.shouldBe(ConstrainedTypeNodeLocations(Location.NoProvided))
                representation.shouldBe("Int")
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(this, testLocation, ConcreteTypeNode("Int", testLocation)),
                    Node(this, testLocation, UnitNode(testLocation))
                )
            )
        }
    }
    "Test Node Interface over TypeNode" {
        TypeNode(
            false,
            null,
            "Num",
            listOf("a"),
            ConcreteTypeNode("Int", testLocation),
            testLocation,
            TypeNodeLocations(
                Location.NoProvided,
                Location.NoProvided,
                Location.NoProvided,
                listOf(),
                Location.NoProvided
            )
        ).node.apply {
            cast<TypeNode>().apply {
                internal.shouldBeFalse()
                annotations.shouldBeNull()
                name.shouldBe("Num")
                params.shouldBe(listOf("a"))
                expr.shouldBe(ConcreteTypeNode("Int", testLocation))
                location.shouldBe(testLocation)
                locations.shouldBe(
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(this, testLocation, ConcreteTypeNode("Int", testLocation)),
                )
            )
        }
    }
    "Test Node interface on UnitTypeNode" {
        UnitTypeNode(
            testLocation
        ).node.apply {
            cast<UnitTypeNode>().apply {
                location.shouldBe(testLocation)
                representation.shouldBe("()")
            }
            parent.shouldBeNull()
            children.toList().shouldBeEmpty()
        }
    }
    "Test Node interface on TypeDeclarationNode" {
        TypeDeclarationNode(
            null,
            "sum",
            listOf(),
            ConcreteTypeNode("Int", testLocation),
            testLocation,
            TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
        ).node.apply {
            cast<TypeDeclarationNode>().apply {
                name.shouldBe("sum")
                type.shouldBe(ConcreteTypeNode("Int", testLocation))
                location.shouldBe(testLocation)
                locations.shouldBe(TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf()))
            }
            parent.shouldBeNull()
            children.toList().shouldBe(
                listOf(
                    Node(this, testLocation, ConcreteTypeNode("Int", testLocation)),
                )
            )
        }
    }
})
