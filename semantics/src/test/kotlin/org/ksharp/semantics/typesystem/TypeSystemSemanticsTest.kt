package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.nodes.*
import org.ksharp.semantics.scopes.TableErrorCode
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.TypeSystemErrorCode

private fun module(vararg types: NodeData) =
    ModuleNode(
        "module",
        listOf(),
        listOf(*types),
        listOf(),
        listOf(),
        Location.NoProvided
    )

class TypeSystemSemanticsTest : StringSpec({
    "Alias semantics" {
        module(
            TypeNode(
                false,
                "Integer",
                listOf(),
                ConcreteTypeNode("Int", Location.NoProvided),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBeEmpty()
            typeSystemTable["Integer"]!!
                .apply {
                    isInternal.shouldBeFalse()
                    isPublic.shouldBeTrue()
                }
            typeSystem["Int"].map { it.representation }.shouldBeRight("(Num numeric<Int>)")
            typeSystem["Integer"].map { it.representation }.shouldBeRight("Int")
        }
    }
    "Alias semantics with Unit" {
        module(
            TypeNode(
                false,
                "Unidad",
                listOf(),
                UnitTypeNode(Location.NoProvided),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBeEmpty()
            typeSystem["Unidad"].map { it.representation }.shouldBeRight("Unit")
        }
    }
    "Tuple semantics" {
        module(
            TypeNode(
                true,
                "Point",
                listOf(),
                TupleTypeNode(
                    listOf(
                        ConcreteTypeNode("Double", Location.NoProvided),
                        ConcreteTypeNode("Double", Location.NoProvided)
                    ),
                    Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBeEmpty()
            typeSystemTable["Point"]!!
                .apply {
                    isInternal.shouldBeTrue()
                    isPublic.shouldBeFalse()
                }
            typeSystem["Point"].map { it.representation }.shouldBeRight("(Double, Double)")
        }
    }
    "Union semantics" {
        module(
            TypeNode(
                false,
                "Maybe",
                listOf("a"),
                UnionTypeNode(
                    listOf(
                        ParametricTypeNode(
                            listOf(
                                ConcreteTypeNode(
                                    "Just", Location.NoProvided
                                ), ParameterTypeNode(
                                    "a", Location.NoProvided
                                )
                            ), Location.NoProvided
                        ),
                        ConcreteTypeNode(
                            "Nothing", Location.NoProvided
                        )
                    ), Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBeEmpty()
            typeSystem["Maybe"].map { it.representation }.shouldBeRight("Just a\n|Nothing")
        }
    }
    "Union semantics arm not starting with concrete type " {
        module(
            TypeNode(
                false,
                "Maybe",
                listOf("a"),
                UnionTypeNode(
                    listOf(
                        ParameterTypeNode(
                            "a", Location.NoProvided
                        ),
                        ConcreteTypeNode(
                            "Nothing", Location.NoProvided
                        )
                    ), Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBe(listOf(TypeSemanticsErrorCode.UnionTypeArmShouldStartWithName.new(Location.NoProvided)))
            typeSystem["Maybe"].shouldBeLeft(
                TypeSystemErrorCode.TypeNotFound.new(
                    "type" to "Maybe"
                )
            )
        }
    }
    "Union semantics param not defined" {
        module(
            TypeNode(
                false,
                "Maybe",
                listOf(),
                UnionTypeNode(
                    listOf(
                        ParametricTypeNode(
                            listOf(
                                ConcreteTypeNode(
                                    "Just", Location.NoProvided
                                ), ParameterTypeNode(
                                    "a", Location.NoProvided
                                )
                            ), Location.NoProvided
                        ),
                        ConcreteTypeNode(
                            "Nothing", Location.NoProvided
                        )
                    ), Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBe(
                listOf(
                    TypeSemanticsErrorCode.ParamNameNoDefined.new(
                        Location.NoProvided,
                        "name" to "a",
                        "type" to "Maybe"
                    )
                )
            )
            typeSystem["Maybe"].shouldBeLeft(
                TypeSystemErrorCode.TypeNotFound.new(
                    "type" to "Maybe"
                )
            )
        }
    }
    "Union semantics invalid arm" {
        module(
            TypeNode(
                false,
                "Maybe",
                listOf("a"),
                UnionTypeNode(
                    listOf(
                        ParametricTypeNode(
                            listOf(
                                ConcreteTypeNode(
                                    "Just", Location.NoProvided
                                ), ParameterTypeNode(
                                    "a", Location.NoProvided
                                )
                            ), Location.NoProvided
                        ),
                        TupleTypeNode(
                            listOf(
                                ConcreteTypeNode(
                                    "Nothing", Location.NoProvided
                                ),
                                ConcreteTypeNode(
                                    "Name", Location.NoProvided
                                )
                            ),
                            Location.NoProvided
                        )
                    ), Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBe(
                listOf(
                    TypeSemanticsErrorCode.InvalidUnionArm.new(
                        Location.NoProvided
                    )
                )
            )
            typeSystem["Maybe"].shouldBeLeft(
                TypeSystemErrorCode.TypeNotFound.new(
                    "type" to "Maybe"
                )
            )
        }
    }
    "Union semantics parametric arm starting with parameter" {
        module(
            TypeNode(
                false,
                "Maybe",
                listOf("a"),
                UnionTypeNode(
                    listOf(
                        ParametricTypeNode(
                            listOf(
                                ConcreteTypeNode(
                                    "Just", Location.NoProvided
                                ), ParameterTypeNode(
                                    "a", Location.NoProvided
                                )
                            ), Location.NoProvided
                        ),
                        ParametricTypeNode(
                            listOf(
                                ParameterTypeNode(
                                    "a", Location.NoProvided
                                ),
                                ConcreteTypeNode(
                                    "Name", Location.NoProvided
                                )
                            ),
                            Location.NoProvided
                        )
                    ), Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBe(listOf(TypeSemanticsErrorCode.UnionTypeArmShouldStartWithName.new(Location.NoProvided)))
            typeSystem["Maybe"].shouldBeLeft(
                TypeSystemErrorCode.TypeNotFound.new(
                    "type" to "Maybe"
                )
            )
        }
    }
    "Type semantics param already defined" {
        module(
            TypeNode(
                false,
                "Maybe",
                listOf("a", "a"),
                UnionTypeNode(
                    listOf(
                        ParametricTypeNode(
                            listOf(
                                ConcreteTypeNode(
                                    "Just", Location.NoProvided
                                ), ParameterTypeNode(
                                    "a", Location.NoProvided
                                )
                            ), Location.NoProvided
                        ),
                        ConcreteTypeNode(
                            "Nothing", Location.NoProvided
                        )
                    ), Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBe(
                listOf(
                    TypeSemanticsErrorCode.ParamNameAlreadyDefined.new(
                        Location.NoProvided,
                        "name" to "a",
                        "type" to "Maybe"
                    )
                )
            )
        }
    }
    "Type semantics parameters not used" {
        module(
            TypeNode(
                false,
                "Maybe",
                listOf("a", "b", "c"),
                UnionTypeNode(
                    listOf(
                        ParametricTypeNode(
                            listOf(
                                ConcreteTypeNode(
                                    "Just", Location.NoProvided
                                ), ParameterTypeNode(
                                    "a", Location.NoProvided
                                )
                            ), Location.NoProvided
                        ),
                        ConcreteTypeNode(
                            "Nothing", Location.NoProvided
                        )
                    ), Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBe(
                listOf(
                    TypeSemanticsErrorCode.ParametersNotUsed.new(
                        Location.NoProvided,
                        "params" to "b, c",
                        "type" to "Maybe"
                    )
                )
            )
        }
    }
    "check Type already defined" {
        module(
            TypeNode(
                false,
                "Integer",
                listOf(),
                ConcreteTypeNode("Int", Location.NoProvided),
                Location.NoProvided
            ),
            TypeNode(
                true,
                "Integer",
                listOf(),
                ConcreteTypeNode("Long", Location.NoProvided),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBe(
                listOf(
                    TableErrorCode.AlreadyDefined.new(Location.NoProvided, "classifier" to "Type", "name" to "Integer")
                )
            )
            typeSystemTable["Integer"]!!
                .apply {
                    isInternal.shouldBeFalse()
                    isPublic.shouldBeTrue()
                }
            typeSystem["Integer"].map { it.representation }.shouldBeRight("Int")
        }
    }
    "Parametric semantics" {
        module(
            TypeNode(
                false,
                "KVStore",
                listOf("k", "v"),
                ParametricTypeNode(
                    listOf(
                        ConcreteTypeNode("Map", Location.NoProvided),
                        ParameterTypeNode("k", Location.NoProvided),
                        ParameterTypeNode("v", Location.NoProvided)
                    ), Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBeEmpty()
            typeSystem["KVStore"].map { it.representation }.shouldBeRight("(Map k v)")
        }
    }
    "Parametric Semantics should start with a concrete type" {
        module(
            TypeNode(
                false,
                "Number",
                listOf("n"),
                ParameterTypeNode("n", Location.NoProvided),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBe(
                listOf(
                    TypeSemanticsErrorCode.TypeShouldStartWithName.new(Location.NoProvided)
                )
            )
            typeSystem["Number"].shouldBeLeft(
                TypeSystemErrorCode.TypeNotFound.new(
                    "type" to "Number"
                )
            )
        }
    }
    "Parametric Semantics should start with a concrete type 2" {
        module(
            TypeNode(
                false,
                "Number",
                listOf("n"),
                ParametricTypeNode(
                    listOf(
                        ParameterTypeNode("n", Location.NoProvided),
                        ConcreteTypeNode("String", Location.NoProvided)
                    ),
                    Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBe(
                listOf(
                    TypeSemanticsErrorCode.TypeShouldStartWithName.new(Location.NoProvided)
                )
            )
            typeSystem["Number"].shouldBeLeft(
                TypeSystemErrorCode.TypeNotFound.new(
                    "type" to "Number"
                )
            )
        }
    }
    "Function Semantics" {
        module(
            TypeNode(
                false,
                "Sum",
                listOf("a"),
                FunctionTypeNode(
                    listOf(
                        ParameterTypeNode("a", Location.NoProvided),
                        ParameterTypeNode("a", Location.NoProvided),
                        ParameterTypeNode("a", Location.NoProvided)
                    ),
                    Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBeEmpty()
            typeSystem["Sum"].map { it.representation }.shouldBeRight("(a -> a -> a)")
        }
    }
    "Function Semantics with concrete types" {
        module(
            TypeNode(
                false,
                "ToString",
                listOf("a"),
                FunctionTypeNode(
                    listOf(
                        ParameterTypeNode("a", Location.NoProvided),
                        ConcreteTypeNode("String", Location.NoProvided)
                    ),
                    Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBeEmpty()
            typeSystem["ToString"].map { it.representation }.shouldBeRight("(a -> String)")
        }
    }
    "public Trait Semantics" {
        module(
            TraitNode(
                false,
                "Number",
                listOf("a"),
                TraitFunctionsNode(
                    listOf(
                        TraitFunctionNode(
                            "sum",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
                                ),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        ),
                        TraitFunctionNode(
                            "prod",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
                                ),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        )
                    )
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBeEmpty()
            typeSystemTable["Number"]!!.apply {
                isInternal.shouldBeFalse()
                isPublic.shouldBeTrue()
            }
            typeSystem["Number"].map { it.representation }.shouldBeRight(
                """
                |trait Number a =
                |    sum :: a -> a -> a
                |    prod :: a -> a -> a
            """.trimMargin()
            )
        }
    }
    "internal Trait Semantics" {
        module(
            TraitNode(
                true,
                "Number",
                listOf("a"),
                TraitFunctionsNode(
                    listOf(
                        TraitFunctionNode(
                            "sum",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
                                ),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        ),
                        TraitFunctionNode(
                            "prod",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
                                ),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        )
                    )
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBeEmpty()
            typeSystemTable["Number"]!!.apply {
                isInternal.shouldBeTrue()
                isPublic.shouldBeFalse()
            }
            typeSystem["Number"].map { it.representation }.shouldBeRight(
                """
                |trait Number a =
                |    sum :: a -> a -> a
                |    prod :: a -> a -> a
            """.trimMargin()
            )
        }
    }
    "Trait Semantics should have just one parameter" {
        module(
            TraitNode(
                false,
                "Number",
                listOf("a", "b"),
                TraitFunctionsNode(
                    listOf(
                        TraitFunctionNode(
                            "sum",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
                                ),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        ),
                        TraitFunctionNode(
                            "prod",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
                                ),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        )
                    )
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBe(
                listOf(
                    TypeSemanticsErrorCode.TraitShouldHaveJustOneParameter
                        .new(Location.NoProvided, "name" to "Number")
                )
            )
            typeSystem["Number"].shouldBeLeft(
                TypeSystemErrorCode.TypeNotFound.new(
                    "type" to "Number"
                )
            )
        }
    }
    "Trait Semantics method fails checkParameters" {
        module(
            TraitNode(
                false,
                "Number",
                listOf("a"),
                TraitFunctionsNode(
                    listOf(
                        TraitFunctionNode(
                            "sum",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("b", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
                                ),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        ),
                        TraitFunctionNode(
                            "prod",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
                                ),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        )
                    )
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBe(
                listOf(
                    TypeSemanticsErrorCode.ParamNameNoDefinedInMethod.new(
                        Location.NoProvided,
                        "name" to "b",
                        "type" to "sum"
                    ),
                    TypeSemanticsErrorCode.TraitWithInvalidMethod.new(
                        Location.NoProvided,
                        "name" to "Number"
                    )
                )
            )
            typeSystem["Number"].shouldBeLeft(
                TypeSystemErrorCode.TypeNotFound.new(
                    "type" to "Number"
                )
            )
        }
    }
    "Trait Semantics method fails parameter not used" {
        module(
            TraitNode(
                false,
                "Number",
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
                        ),
                        TraitFunctionNode(
                            "prod",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
                                ),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        )
                    )
                ),
                Location.NoProvided
            )
        ).checkSemantics().apply {
            errors.shouldBe(
                listOf(
                    TypeSemanticsErrorCode.ParametersNotUsedInMethod.new(
                        Location.NoProvided,
                        "params" to "a",
                        "type" to "sum"
                    ),
                    TypeSemanticsErrorCode.TraitWithInvalidMethod.new(
                        Location.NoProvided,
                        "name" to "Number"
                    )
                )
            )
            typeSystem["Number"].shouldBeLeft(
                TypeSystemErrorCode.TypeNotFound.new(
                    "type" to "Number"
                )
            )
        }
    }
})