package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.*
import org.ksharp.semantics.expressions.FunctionSemanticsErrorCode
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.TypeSystemErrorCode
import org.ksharp.typesystem.types.Type

private fun module(vararg types: NodeData) =
    ModuleNode(
        "module",
        listOf(),
        listOf(*types),
        listOf(),
        listOf(),
        listOf(),
        Location.NoProvided
    )

private fun moduleWithDeclarations(vararg declarations: TypeDeclarationNode) =
    ModuleNode(
        "module",
        listOf(),
        listOf(),
        listOf(*declarations),
        listOf(),
        listOf(),
        Location.NoProvided
    )

private val Type.representationWithVisibility get() = "${visibility.name}-${representation}"

class TypeSystemSemanticsTest : StringSpec({
    "Type annotation semantics" {
        module(
            TypeNode(
                false,
                listOf(
                    AnnotationNode(
                        "native", mapOf("flag" to true), Location.NoProvided,
                        AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                    )
                ),
                "Integer",
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
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Int"].map { it.representation }.shouldBeRight("(Num NativeInt)")
            typeSystem["Integer"].map { it.representationWithVisibility }
                .shouldBeRight("Public-@native(flag=true) Int")
        }
    }
    "Alias semantics" {
        module(
            TypeNode(
                false,
                null,
                "Integer",
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
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Int"].map { it.representation }.shouldBeRight("(Num NativeInt)")
            typeSystem["Integer"].map { it.representationWithVisibility }
                .shouldBeRight("Public-Int")
        }
    }
    "Alias semantics with Unit" {
        module(
            TypeNode(
                false,
                null,
                "Unidad",
                listOf(),
                UnitTypeNode(Location.NoProvided),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Unidad"].map { it.representationWithVisibility }
                .shouldBeRight("Public-Unit")
        }
    }
    "Tuple semantics" {
        module(
            TypeNode(
                true,
                null,
                "Point",
                listOf(),
                TupleTypeNode(
                    listOf(
                        ConcreteTypeNode("Double", Location.NoProvided),
                        ConcreteTypeNode("Double", Location.NoProvided)
                    ),
                    Location.NoProvided,
                    TupleTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Point"].map { it.representationWithVisibility }
                .shouldBeRight("Internal-(Double, Double)")
        }
    }
    "Union semantics" {
        module(
            TypeNode(
                false,
                null,
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
                    ), Location.NoProvided,
                    UnionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Maybe"]
                .map { it.representationWithVisibility }
                .shouldBeRight("Public-Just a\n|Nothing")
        }
    }
    "Union semantics arm not starting with concrete type " {
        module(
            TypeNode(
                false,
                null,
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
                    ), Location.NoProvided,
                    UnionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
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
                null,
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
                    ), Location.NoProvided, UnionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
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
                null,
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
                            Location.NoProvided, TupleTypeNodeLocations(listOf())
                        )
                    ), Location.NoProvided, UnionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
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
                null,
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
                    ), Location.NoProvided, UnionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
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
                null,
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
                    ), Location.NoProvided, UnionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
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
                null,
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
                    ), Location.NoProvided, UnionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
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
                null,
                "Integer",
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
                true,
                null,
                "Integer",
                listOf(),
                ConcreteTypeNode("Long", Location.NoProvided),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBe(
                listOf(
                    TypeSystemErrorCode.TypeAlreadyRegistered.new(
                        "type" to "Integer"
                    )
                )
            )
            typeSystem["Integer"].map { it.representationWithVisibility }.shouldBeRight("Public-Int")
        }
    }
    "Parametric semantics" {
        module(
            TypeNode(
                false,
                null,
                "KVStore",
                listOf("k", "v"),
                ParametricTypeNode(
                    listOf(
                        ConcreteTypeNode("Map", Location.NoProvided),
                        ParameterTypeNode("k", Location.NoProvided),
                        ParameterTypeNode("v", Location.NoProvided)
                    ), Location.NoProvided
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["KVStore"].map { it.representation }.shouldBeRight("(Map k v)")
        }
    }
    "Parametric Semantics should start with a concrete type" {
        module(
            TypeNode(
                false,
                null,
                "Number",
                listOf("n"),
                ParameterTypeNode("n", Location.NoProvided),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
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
                null,
                "Number",
                listOf("n"),
                ParametricTypeNode(
                    listOf(
                        ParameterTypeNode("n", Location.NoProvided),
                        ConcreteTypeNode("String", Location.NoProvided)
                    ),
                    Location.NoProvided
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
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
                null,
                "Sum",
                listOf("a"),
                FunctionTypeNode(
                    listOf(
                        ParameterTypeNode("a", Location.NoProvided),
                        ParameterTypeNode("a", Location.NoProvided),
                        ParameterTypeNode("a", Location.NoProvided)
                    ),
                    Location.NoProvided,
                    FunctionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Sum"].map { it.representation }.shouldBeRight("(a -> a -> a)")
        }
    }
    "Function Semantics with concrete types" {
        module(
            TypeNode(
                false,
                null,
                "ToString",
                listOf("a"),
                FunctionTypeNode(
                    listOf(
                        ParameterTypeNode("a", Location.NoProvided),
                        ConcreteTypeNode("String", Location.NoProvided)
                    ),
                    Location.NoProvided,
                    FunctionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["ToString"].map { it.representation }.shouldBeRight("(a -> String)")
        }
    }
    "Trait with Annotations Semantics" {
        module(
            TraitNode(
                false,
                listOf(
                    AnnotationNode(
                        "native", mapOf(), Location.NoProvided,
                        AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                    )
                ),
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
                                Location.NoProvided, FunctionTypeNodeLocations(listOf())
                            ),
                            Location.NoProvided, TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
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
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Number"].map { it.representationWithVisibility }.shouldBeRight(
                """
                |Public-@native trait Number a =
                |    sum :: a -> a -> a
            """.trimMargin()
            )
        }
    }
    "public Trait Semantics" {
        module(
            TraitNode(
                false,
                null,
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
                                Location.NoProvided,
                                FunctionTypeNodeLocations(listOf())
                            ),
                            Location.NoProvided,
                            TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                        ),
                        TraitFunctionNode(
                            "prod",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
                                ),
                                Location.NoProvided,
                                FunctionTypeNodeLocations(listOf())
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
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Number"].map { it.representationWithVisibility }.shouldBeRight(
                """
                |Public-trait Number a =
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
                null,
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
                                Location.NoProvided, FunctionTypeNodeLocations(listOf())
                            ),
                            Location.NoProvided, TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                        ),
                        TraitFunctionNode(
                            "prod",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
                                ),
                                Location.NoProvided, FunctionTypeNodeLocations(listOf())
                            ),
                            Location.NoProvided, TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
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
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Number"].map { it.representationWithVisibility }.shouldBeRight(
                """
                |Internal-trait Number a =
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
                null,
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
                                Location.NoProvided,
                                FunctionTypeNodeLocations(listOf())
                            ),
                            Location.NoProvided,
                            TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                        ),
                        TraitFunctionNode(
                            "prod",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
                                ),
                                Location.NoProvided,
                                FunctionTypeNodeLocations(listOf())
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
        ).checkTypesSemantics(preludeModule).apply {
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
                null,
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
                                Location.NoProvided,
                                FunctionTypeNodeLocations(listOf())
                            ),
                            Location.NoProvided,
                            TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                        ),
                        TraitFunctionNode(
                            "prod",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
                                ),
                                Location.NoProvided,
                                FunctionTypeNodeLocations(listOf())
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
        ).checkTypesSemantics(preludeModule).apply {
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
                null,
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
                                Location.NoProvided, FunctionTypeNodeLocations(listOf())
                            ),
                            Location.NoProvided,
                            TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                        ),
                        TraitFunctionNode(
                            "prod",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
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
        ).checkTypesSemantics(preludeModule).apply {
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
    "Interceptor types semantics" {
        module(
            TraitNode(
                false,
                null,
                "EqTest",
                listOf("a"),
                TraitFunctionsNode(
                    listOf(
                        TraitFunctionNode(
                            "eq",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ConcreteTypeNode("Bool", Location.NoProvided)
                                ),
                                Location.NoProvided,
                                FunctionTypeNodeLocations(listOf())
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
            ),
            TraitNode(
                false,
                null,
                "OrdTest",
                listOf("a"),
                TraitFunctionsNode(
                    listOf(
                        TraitFunctionNode(
                            "eq",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ConcreteTypeNode("Int", Location.NoProvided)
                                ),
                                Location.NoProvided,
                                FunctionTypeNodeLocations(listOf())
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
            ),
            TypeNode(
                false,
                null,
                "Number",
                listOf(),
                IntersectionTypeNode(
                    listOf(
                        ConcreteTypeNode(
                            "EqTest", Location.NoProvided
                        ),
                        ConcreteTypeNode(
                            "OrdTest", Location.NoProvided
                        )
                    ), Location.NoProvided, IntersectionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Number"].map { it.representation }.shouldBeRight("(EqTest & OrdTest)")
        }
    }
    "Interceptor types semantics invalid type arm" {
        module(
            TraitNode(
                false,
                null,
                "EqTest",
                listOf("a"),
                TraitFunctionsNode(
                    listOf(
                        TraitFunctionNode(
                            "eq",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ConcreteTypeNode("Bool", Location.NoProvided)
                                ),
                                Location.NoProvided, FunctionTypeNodeLocations(listOf())
                            ),
                            Location.NoProvided, TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
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
            ),
            TraitNode(
                false,
                null,
                "OrdTest",
                listOf("a"),
                TraitFunctionsNode(
                    listOf(
                        TraitFunctionNode(
                            "eq",
                            FunctionTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ConcreteTypeNode("Int", Location.NoProvided)
                                ),
                                Location.NoProvided, FunctionTypeNodeLocations(listOf())
                            ),
                            Location.NoProvided, TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
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
            ),
            TypeNode(
                false,
                null,
                "Number",
                listOf("a"),
                IntersectionTypeNode(
                    listOf(
                        ConcreteTypeNode(
                            "EqTest", Location.NoProvided
                        ),
                        ConcreteTypeNode(
                            "OrdTest", Location.NoProvided
                        ),
                        ParameterTypeNode("a", Location.NoProvided),
                    ), Location.NoProvided, IntersectionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBe(
                listOf(
                    TypeSemanticsErrorCode.InterceptorTypeWithInvalidType.new(Location.NoProvided, "name" to "Number")
                )
            )
            typeSystem["Number"].shouldBeLeft(TypeSystemErrorCode.TypeNotFound.new("type" to "Number"))
        }
    }
    "Label semantics" {
        module(
            TypeNode(
                false,
                null,
                "KVStore",
                listOf("k", "v"),
                ParametricTypeNode(
                    listOf(
                        ConcreteTypeNode("Map", Location.NoProvided),
                        LabelTypeNode(
                            "key", ParameterTypeNode("k", Location.NoProvided), Location.NoProvided
                        ),
                        LabelTypeNode(
                            "value", ParameterTypeNode("v", Location.NoProvided), Location.NoProvided
                        )
                    ), Location.NoProvided
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["KVStore"].map { it.representation }.shouldBeRight("(Map key: k value: v)")
        }
    }
    "Label semantics on tuples" {
        module(
            TypeNode(
                false,
                null, "Point2D", listOf(), TupleTypeNode(
                    listOf(
                        LabelTypeNode(
                            "x",
                            ConcreteTypeNode("Double", Location.NoProvided),
                            Location.NoProvided
                        ),
                        LabelTypeNode(
                            "y",
                            ConcreteTypeNode("Double", Location.NoProvided),
                            Location.NoProvided
                        )
                    ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                ), Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Point2D"].map { it.representation }.shouldBeRight("(x: Double, y: Double)")
        }
    }
    "Label semantics on composite types" {
        module(
            TypeNode(
                false,
                null,
                "Composite",
                listOf("a"),
                TupleTypeNode(
                    listOf(
                        LabelTypeNode(
                            "n", ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode("Num", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided)
                                ), Location.NoProvided
                            ), Location.NoProvided
                        ),
                        LabelTypeNode(
                            "point", TupleTypeNode(
                                listOf(
                                    LabelTypeNode(
                                        "x",
                                        ConcreteTypeNode("Double", Location.NoProvided),
                                        Location.NoProvided
                                    ),
                                    LabelTypeNode(
                                        "y",
                                        ConcreteTypeNode("Double", Location.NoProvided),
                                        Location.NoProvided
                                    )
                                ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                            ), Location.NoProvided
                        )
                    ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Composite"].map { it.representation }
                .shouldBeRight("(n: (Num a), point: (x: Double, y: Double))")
        }
    }
    "Composite semantics with Unit type" {
        module(
            TypeNode(
                false,
                null, "Composite", listOf(), TupleTypeNode(
                    listOf(
                        LabelTypeNode(
                            "n",
                            UnitTypeNode(Location.NoProvided),
                            Location.NoProvided
                        ),
                        LabelTypeNode(
                            "point", TupleTypeNode(
                                listOf(
                                    LabelTypeNode(
                                        "x",
                                        ConcreteTypeNode("Double", Location.NoProvided),
                                        Location.NoProvided
                                    ),
                                    LabelTypeNode(
                                        "y",
                                        ConcreteTypeNode("Double", Location.NoProvided),
                                        Location.NoProvided
                                    )
                                ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                            ), Location.NoProvided
                        )
                    ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                ), Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Composite"].map { it.representation }.shouldBeRight("(n: Unit, point: (x: Double, y: Double))")
        }
    }
    "Composite semantics with function types" {
        module(
            TypeNode(
                false,
                null,
                "Composite",
                listOf(),
                TupleTypeNode(
                    listOf(
                        LabelTypeNode(
                            "n", FunctionTypeNode(
                                listOf(
                                    ConcreteTypeNode("Int", Location.NoProvided),
                                    ConcreteTypeNode("Int", Location.NoProvided)
                                ), Location.NoProvided, FunctionTypeNodeLocations(listOf())
                            ), Location.NoProvided
                        ),
                        LabelTypeNode(
                            "point", TupleTypeNode(
                                listOf(
                                    LabelTypeNode(
                                        "x",
                                        ConcreteTypeNode("Double", Location.NoProvided),
                                        Location.NoProvided
                                    ),
                                    LabelTypeNode(
                                        "y",
                                        ConcreteTypeNode("Double", Location.NoProvided),
                                        Location.NoProvided
                                    )
                                ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                            ), Location.NoProvided
                        )
                    ), Location.NoProvided,
                    TupleTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Composite"].map { it.representation }
                .shouldBeRight("(n: (Int -> Int), point: (x: Double, y: Double))")
        }
    }
    "Composite semantics parametric type starting with parameter" {
        module(
            TypeNode(
                false,
                null, "Composite", listOf("a"), TupleTypeNode(
                    listOf(
                        LabelTypeNode(
                            "n", ParametricTypeNode(
                                listOf(
                                    ParameterTypeNode("a", Location.NoProvided),
                                    ConcreteTypeNode("Int", Location.NoProvided)
                                ), Location.NoProvided
                            ), Location.NoProvided
                        ),
                        LabelTypeNode(
                            "point", TupleTypeNode(
                                listOf(
                                    LabelTypeNode(
                                        "x",
                                        ConcreteTypeNode("Double", Location.NoProvided),
                                        Location.NoProvided
                                    ),
                                    LabelTypeNode(
                                        "y",
                                        ConcreteTypeNode("Double", Location.NoProvided),
                                        Location.NoProvided
                                    )
                                ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                            ), Location.NoProvided
                        )
                    ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                ), Location.NoProvided,
                TypeNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBe(listOf(TypeSemanticsErrorCode.ParametricTypeShouldStartWithName.new(Location.NoProvided)))
            typeSystem["Composite"].shouldBeLeft(TypeSystemErrorCode.TypeNotFound.new("type" to "Composite"))
        }
    }
    "Function declaration with annotation semantics" {
        moduleWithDeclarations(
            TypeDeclarationNode(
                listOf(
                    AnnotationNode(
                        "Test", mapOf(), Location.NoProvided,
                        AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                    )
                ),
                "ten",
                listOf(),
                FunctionTypeNode(
                    listOf(UnitTypeNode(Location.NoProvided), ConcreteTypeNode("Int", Location.NoProvided)),
                    Location.NoProvided,
                    FunctionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Decl__ten"].map { it.representation }
                .shouldBeRight("@Test Unit -> Int")
        }
    }
    "Function declaration semantics" {
        moduleWithDeclarations(
            TypeDeclarationNode(
                null,
                "ten",
                listOf(),
                FunctionTypeNode(
                    listOf(UnitTypeNode(Location.NoProvided), ConcreteTypeNode("Int", Location.NoProvided)),
                    Location.NoProvided,
                    FunctionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Decl__ten"].map { it.representation }
                .shouldBeRight("(Unit -> Int)")
        }
    }
    "Function declaration semantics no function literal" {
        moduleWithDeclarations(
            TypeDeclarationNode(
                null,
                "ten",
                listOf(),
                ConcreteTypeNode("Int", Location.NoProvided),
                Location.NoProvided,
                TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBe(
                listOf(
                    TypeSemanticsErrorCode.FunctionDeclarationShouldBeAFunctionType.new(
                        Location.NoProvided,
                        "name" to "ten",
                        "repr" to "Int"
                    )
                )
            )
        }
    }
    "Function declaration with params" {
        moduleWithDeclarations(
            TypeDeclarationNode(
                null,
                "sum",
                listOf("a"),
                FunctionTypeNode(
                    listOf(
                        ParametricTypeNode(
                            listOf(
                                ConcreteTypeNode("Num", Location.NoProvided),
                                ParameterTypeNode("a", Location.NoProvided),
                            ),
                            Location.NoProvided
                        ),
                        ParametricTypeNode(
                            listOf(
                                ConcreteTypeNode("Num", Location.NoProvided),
                                ParameterTypeNode("a", Location.NoProvided),
                            ),
                            Location.NoProvided
                        ),
                        ConcreteTypeNode("Int", Location.NoProvided)
                    ),
                    Location.NoProvided,
                    FunctionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBeEmpty()
            typeSystem["Decl__sum"].map { it.representation }
                .shouldBeRight("((Num a) -> (Num a) -> Int)")
        }
    }
    "Function declaration with invalid name" {
        moduleWithDeclarations(
            TypeDeclarationNode(
                null,
                "dot.sum",
                listOf("a"),
                FunctionTypeNode(
                    listOf(
                        ParametricTypeNode(
                            listOf(
                                ConcreteTypeNode("Num", Location.NoProvided),
                                ParameterTypeNode("a", Location.NoProvided),
                            ),
                            Location.NoProvided
                        ),
                        ParametricTypeNode(
                            listOf(
                                ConcreteTypeNode("Num", Location.NoProvided),
                                ParameterTypeNode("a", Location.NoProvided),
                            ),
                            Location.NoProvided
                        ),
                        ConcreteTypeNode("Int", Location.NoProvided)
                    ),
                    Location.NoProvided,
                    FunctionTypeNodeLocations(listOf())
                ),
                Location.NoProvided,
                TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
            )
        ).checkTypesSemantics(preludeModule).apply {
            errors.shouldBe(
                listOf(FunctionSemanticsErrorCode.InvalidFunctionName.new(Location.NoProvided, "name" to "dot.sum"))
            )
            typeSystem["Decl__sum"].shouldBeLeft()
        }
    }
})
