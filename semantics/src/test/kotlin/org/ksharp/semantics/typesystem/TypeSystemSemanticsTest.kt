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

private fun module(vararg types: TypeNode) =
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
})