package org.ksharp.typesystem

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.common.new
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.annotations.annotation
import org.ksharp.typesystem.types.*

fun ErrorOrType.shouldBeType(type: Type, repr: String) =
    this.apply {
        map { it.representation.also(::println) }.shouldBeRight(repr)
        shouldBeRight(type)
    }

class TypeSystemTest : ShouldSpec({
    context("Given a type system. Check:") {
        context("Annotated types") {
            val annotations = listOf(
                annotation("impure") {
                    set("lang", "kotlin")
                }
            )
            typeSystem {
                type("Int", annotations)
                alias("Integer", annotations) {
                    type("Int")
                }
                parametricType("List", annotations) {
                    parameter("a")
                }
            }.apply {
                context("Should contains the following types:") {
                    should("Int type") {
                        get("Int").shouldBeType(Annotated(annotations, Concrete("Int")), "@impure(lang=kotlin) Int")
                    }
                    should("Integer type") {
                        get("Integer").shouldBeType(Annotated(annotations, Concrete("Int")), "@impure(lang=kotlin) Int")
                    }
                    should("List type") {
                        get("List").shouldBeType(
                            Annotated(
                                annotations,
                                ParametricType(Concrete("List"), listOf(Parameter("a")))
                            ), "@impure(lang=kotlin) List a"
                        )
                    }
                }
                should("Should have 3 types") {
                    size.shouldBe(3)
                }
                should("Shouldn't have errors") {
                    errors.shouldBeEmpty()
                }
            }
        }
        context("Concrete, Aliases, Parametric and Function Types") {
            typeSystem {
                type("Int")
                type("String")
                alias("Integer") {
                    type("Int")
                }
                parametricType("Map") {
                    parameter("a")
                    parameter("b")
                }
                alias("StringMap") {
                    parametricType("Map") {
                        type("String")
                        parameter("b")
                    }
                }
                alias("Sum") {
                    functionType {
                        type("Int")
                        type("Int")
                        type("Int")
                    }
                } //.map { it.representation }.shouldBeRight("(Int -> Int -> Int)")
            }.apply {
                context("Should contains the following types:") {
                    should("Int type") {
                        get("Int").shouldBeType(Concrete("Int"), "Int")
                    }
                    should("String type") {
                        get("String").shouldBeType(Concrete("String"), "String")
                    }
                    should("Integer alias type") {
                        get("Integer").shouldBeType(Concrete("Int"), "Int")
                    }
                    should("(Map a b) parametric type") {
                        get("Map").shouldBeType(
                            ParametricType(
                                Concrete("Map"),
                                listOf(
                                    Parameter("a"),
                                    Parameter("b")
                                )
                            ), "(Map a b)"
                        )
                    }
                    should("StringMap should be alias of (Map String b)") {
                        get("StringMap").shouldBeType(
                            ParametricType(
                                Concrete("Map"),
                                listOf(
                                    Concrete("String"),
                                    Parameter("b")
                                )
                            ), "(Map String b)"
                        )
                    }
                    should("Sum should be function of Int -> Int -> Int") {
                        get("Sum").shouldBeType(
                            FunctionType(
                                listOf(
                                    Concrete("Int"),
                                    Concrete("Int"),
                                    Concrete("Int")
                                )
                            ), "(Int -> Int -> Int)"
                        )
                    }
                }
                should("Should have 6 types") {
                    size.shouldBe(6)
                }
                should("Shouldn't have errors") {
                    errors.shouldBeEmpty()
                }
            }
        }
        context("Parametric Types") {
            typeSystem {
                parametricType("Map") {
                    parameter("a")
                    parameter("b")
                }

                parametricType("Either") {
                    parameter("a")
                    parametricType("Either") {
                        parameter("a")
                        parameter("b")
                    }
                }

                alias("StringMap") {
                    parametricType("Map") {
                        parameter("a")
                    }
                }
            }.apply {
                context("Should contains the following types:") {
                    should("(Map a b) type") {
                        get("Map").shouldBeType(
                            ParametricType(
                                Concrete("Map"),
                                listOf(Parameter("a"), Parameter("b"))
                            ),
                            "(Map a b)"
                        )
                    }
                    should("Recursive: (Either a (Either a b)) type") {
                        get("Either").shouldBeType(
                            ParametricType(
                                Concrete("Either"),
                                listOf(
                                    Parameter("a"),
                                    ParametricType(
                                        Concrete("Either"),
                                        listOf(Parameter("a"), Parameter("b"))
                                    )
                                )
                            ),
                            "(Either a (Either a b))"
                        )
                    }
                }
                should("Should contain 3 types") {
                    size.shouldBe(2)
                }
                should("Should have errors") {
                    errors.shouldBe(
                        listOf(
                            TypeSystemErrorCode.InvalidNumberOfParameters.new(
                                "type" to ParametricType(
                                    Concrete("Map"),
                                    listOf(
                                        Parameter("a"),
                                        Parameter("b")
                                    )
                                ),
                                "number" to 2,
                                "configuredType" to ParametricType(
                                    Concrete("Map"),
                                    listOf(
                                        Parameter("a")
                                    )
                                )
                            )
                        )
                    )
                }
            }
        }
        context("Function types") {
            typeSystem {
                type("Int")
                parametricType("List") {
                    parameter("a")
                }
                alias("Sum") {
                    functionType {
                        type("Int")
                    }
                }
                alias("Sum") {
                    functionType {
                    }
                }
                alias("Sum") {
                    functionType {
                        type("Int")
                        type("Int")
                        type("Int")
                    }
                }
                alias("Abs") {
                    functionType {
                        parameter("a")
                        type("Int")
                    }
                }
                alias("Get") {
                    functionType {
                        parametricType("List") {
                            type("Int")
                        }
                        type("Int")
                    }
                }
                alias("MapFn") {
                    functionType {
                        parametricType("List") {
                            parameter("a")
                        }
                        functionType {
                            parameter("a")
                            parameter("b")
                        }
                        parametricType("List") {
                            parameter("b")
                        }
                    }
                }
            }.apply {
                context("Should contains the following types:") {
                    should("Int type") {
                        get("Int").shouldBeType(Concrete("Int"), "Int")
                    }
                    should("(List a) type") {
                        get("List").shouldBeType(
                            ParametricType(
                                Concrete("List"),
                                listOf(Parameter("a"))
                            ),
                            "(List a)"
                        )
                    }
                    should("(Int -> Int -> Int) type") {
                        get("Sum").shouldBeType(
                            FunctionType(
                                listOf(Concrete("Int"), Concrete("Int"), Concrete("Int"))
                            ),
                            "(Int -> Int -> Int)"
                        )
                    }
                    should("(a -> Int) type") {
                        get("Abs").shouldBeType(
                            FunctionType(listOf(Parameter("a"), Concrete("Int"))),
                            "(a -> Int)"
                        )
                    }
                    should("((List Int) -> Int) type") {
                        get("Get")
                            .shouldBeType(
                                FunctionType(
                                    listOf(
                                        ParametricType(
                                            Concrete("List"),
                                            listOf(Concrete("Int"))
                                        ),
                                        Concrete("Int")
                                    )
                                ),
                                "((List Int) -> Int)"
                            )
                    }
                    should("((List a) -> (a -> b) -> (List b)) type") {
                        get("MapFn")
                            .shouldBeType(
                                FunctionType(
                                    listOf(
                                        ParametricType(
                                            Concrete("List"),
                                            listOf(Parameter("a"))
                                        ),
                                        FunctionType(
                                            listOf(
                                                Parameter("a"),
                                                Parameter("b")
                                            )
                                        ),
                                        ParametricType(
                                            Concrete("List"),
                                            listOf(Parameter("b"))
                                        ),
                                    )
                                ),
                                "((List a) -> (a -> b) -> (List b))"
                            )
                    }
                }
                should("Should have 6 types") {
                    size.shouldBe(6)
                }
                should("Should have 2 errors") {
                    errors.shouldBe(
                        listOf(
                            TypeSystemErrorCode.InvalidFunctionType.new(),
                            TypeSystemErrorCode.InvalidFunctionType.new()
                        )
                    )
                }
            }
        }
        context("Tuple types") {
            typeSystem {
                type("String")
                parametricType("Num") {
                    parameter("a")
                }
                alias("User") {
                    tupleType {
                        type("String")
                        type("String")
                    }
                }
                alias("Point") {
                    tupleType {
                        parametricType("Num") {
                            parameter("x")
                        }
                        parametricType("Num") {
                            parameter("x")
                        }
                    }
                }
            }.apply {
                context("Should contains the following types:") {
                    should("User (String, String) type") {
                        get("User").shouldBeType(
                            TupleType(
                                listOf(Concrete("String"), Concrete("String"))
                            ),
                            "(String, String)"
                        )
                    }
                    should("Point ((Num x), (Num x)) type") {
                        get("Point").shouldBeType(
                            TupleType(
                                listOf(
                                    ParametricType(
                                        Concrete("Num"), listOf(Parameter("x"))
                                    ),
                                    ParametricType(
                                        Concrete("Num"), listOf(Parameter("x"))
                                    ),
                                )
                            ),
                            "((Num x), (Num x))"
                        )
                    }
                }
                should("Should have 4 types") {
                    size.shouldBe(4)
                }
                should("Shouldn't have errors") {
                    errors.shouldBeEmpty()
                }
            }
        }
        context("Union types") {
            typeSystem {
                type("String")
                parametricType("Dict") {
                    type("String")
                    parameter("b")
                }
                alias("Either") {
                    unionType {
                        clazz("Left") {
                            parameter("a")
                        }
                        clazz("Right") {
                            parameter("b")
                        }
                    }
                }
                alias("Weekend") {
                    unionType {
                        clazz("Saturday")
                        clazz("Sunday")
                    }
                }
            }.apply {
                context("Should contains the following types:") {
                    should("String type") {
                        get("String").shouldBeType(Concrete("String"), "String")
                    }
                    should("(Dict String b) type") {
                        get("Dict").shouldBeType(
                            ParametricType(
                                Concrete("Dict"),
                                listOf(Concrete("String"), Parameter("b"))
                            ),
                            "(Dict String b)"
                        )
                    }
                    should("Left a|Right b type") {
                        get("Either").shouldBeType(
                            UnionType(
                                mapOf(
                                    "Left" to UnionType.ClassType("Left", listOf(Parameter("a"))),
                                    "Right" to UnionType.ClassType("Right", listOf(Parameter("b")))
                                )
                            ),
                            "Left a\n|Right b"
                        )
                    }
                    should("Saturday|Sunday type") {
                        get("Weekend").shouldBeType(
                            UnionType(
                                mapOf(
                                    "Saturday" to UnionType.ClassType("Saturday", listOf()),
                                    "Sunday" to UnionType.ClassType("Sunday", listOf())
                                )
                            ),
                            "Saturday\n|Sunday"
                        )
                    }
                }
                should("Should have 4 types") {
                    size.shouldBe(4)
                }
                should("Shouldn't have errors") {
                    errors.shouldBeEmpty()
                }
            }
        }
        context("Trait types") {
            typeSystem {
                trait("Num", "a") {
                    method("(+)") {
                        parameter("a")
                        parameter("a")
                        parameter("a")
                    }
                    method("(-)") {
                        parameter("a")
                        parameter("a")
                        parameter("a")
                    }
                }
                trait("num", "a") {
                    method("(+)") {
                        parameter("a")
                        parameter("a")
                        parameter("a")
                    }
                }
                trait("Functor", "F") {
                    method("map") {
                        functionType {
                            parameter("a")
                            parameter("b")
                        }
                        parameter("b")
                    }
                }
                trait("Monad", "m") {
                    method("map ing") {
                        functionType {
                            parameter("a")
                            parameter("b")
                        }
                        parameter("b")
                    }
                }
            }.apply {
                context("Should contains the following types:") {
                    should("Num trait") {
                        get("Num").shouldBeType(
                            TraitType(
                                "Num", "a", mapOf(
                                    "(+)" to TraitType.MethodType(
                                        "(+)",
                                        listOf(Parameter("a"), Parameter("a"), Parameter("a"))
                                    ),
                                    "(-)" to TraitType.MethodType(
                                        "(-)",
                                        listOf(Parameter("a"), Parameter("a"), Parameter("a"))
                                    )
                                )
                            ), "trait Num a =\n" +
                                    "    (+) :: a -> a -> a\n" +
                                    "    (-) :: a -> a -> a"
                        )
                    }
                }
                should("Should have 1 type") {
                    size.shouldBe(1)
                }
                should("Shouldn't have errors") {
                    errors.shouldBe(
                        listOf(
                            TypeSystemErrorCode.TypeNameShouldStartWithUpperCase.new("name" to "num"),
                            TypeSystemErrorCode.TypeParamNameShouldStartWithLowerCase.new("name" to "F"),
                            TypeSystemErrorCode.FunctionNameShouldntHaveSpaces.new("name" to "map ing"),
                        )
                    )
                }
            }
        }
        context("Intersection types") {
            typeSystem {
                type("Int")
                type("Char")
                alias("OrdNum") {
                    intersectionType {
                        type("Num")
                        type("Ord")
                    }
                }
                trait("Num", "a") {
                    method("(+)") {
                        parameter("a")
                        parameter("a")
                        parameter("a")
                    }
                }
                trait("Ord", "a") {
                    method("(<=)") {
                        parameter("a")
                        parameter("a")
                    }
                }
                alias("Str") {
                    intersectionType {
                        type("Int")
                        type("Char")
                    }
                }
            }.apply {
                context("Should contains the following types:") {
                    should("OrdNum intersection type") {
                        get("OrdNum").shouldBeType(
                            IntersectionType(listOf(Concrete("Num"), Concrete("Ord"))),
                            "(Num & Ord)"
                        )
                    }
                }
                should("Should have 5 types") {
                    size.shouldBe(5)
                }
                should("Should have errors") {
                    errors.shouldBe(
                        listOf(
                            TypeSystemErrorCode.IntersectionTypeShouldBeTraits.new("name" to "Int"),
                            TypeSystemErrorCode.IntersectionTypeShouldBeTraits.new("name" to "Char"),
                        )
                    )
                }
            }
        }
        context("Error validations") {
            typeSystem {
                type("int")
                type("Int")
                alias("PInt") {
                    parametricType("Int") {}
                }
                parametricType("Test") {}
                alias("Float") {
                    type("Double")
                }
                parametricType("Float") {
                    type("Double")
                }
                type("Int")
            }.apply {
                should("Should have Int type") {
                    get("Int").shouldBeType(Concrete("Int"), "Int")
                }
                should("Shouldn't find the type String") {
                    get("String").shouldBeLeft(TypeSystemErrorCode.TypeNotFound.new("type" to "String"))
                }
                should("Should have one type") {
                    size.shouldBe(1)
                }
                should("Should contains errors") {
                    errors.shouldBe(
                        listOf(
                            TypeSystemErrorCode.TypeNameShouldStartWithUpperCase.new("name" to "int"),
                            TypeSystemErrorCode.NoParametrizedType.new("type" to "Int"),
                            TypeSystemErrorCode.ParametricTypeWithoutParameters.new("type" to "Test"),
                            TypeSystemErrorCode.TypeNotFound.new("type" to "Double"),
                            TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to "Float"),
                            TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to "Int")
                        )
                    )
                }
            }
        }
        context("Labels") {
            typeSystem {
                type("String")
                type("Int")
                parametricType("Num") {
                    parameter("x")
                }
                parametricType("Map") {
                    parameter("k", "key")
                    parameter("v", "value")
                }
                alias("User") {
                    tupleType {
                        type("String", "name")
                        type("String", "password")
                    }
                }
                alias("Point") {
                    tupleType {
                        parameter("n", "x")
                        parameter("n", "y")
                    }
                }
                alias("Point2D") {
                    tupleType {
                        parametricType("Num", "x") {
                            parameter("n")
                        }
                        parametricType("Num", "y") {
                            parameter("n")
                        }
                    }
                }
                alias("Sum") {
                    functionType {
                        type("Int", "a")
                        parameter("b")
                        type("Int", "result")
                    }
                }
                alias("Option") {
                    unionType {
                        clazz("Some") {
                            parameter("v", "value")
                        }
                        clazz("None")
                    }
                }
            }.apply {
                context("Should contain the following types:") {
                    should("String type") {
                        get("String").shouldBeType(Concrete("String"), "String")
                    }
                    should("Int type") {
                        get("Int").shouldBeType(Concrete("Int"), "Int")
                    }
                    should("(Num x) type") {
                        get("Num").shouldBeType(ParametricType(Concrete("Num"), listOf(Parameter("x"))), "(Num x)")
                    }
                    should("(Map key: k value: v) type") {
                        get("Map").shouldBeType(
                            ParametricType(
                                Concrete("Map"),
                                listOf(
                                    Parameter("k").labeled("key"),
                                    Parameter("v").labeled("value")
                                )
                            ), "(Map key: k value: v)"
                        )
                    }
                    should("(name: String, password: String) type") {
                        get("User").shouldBeType(
                            TupleType(
                                listOf(
                                    Concrete("String").labeled("name"),
                                    Concrete("String").labeled("password")
                                )
                            ),
                            "(name: String, password: String)"
                        )
                    }
                    should("(x: n, y: n) type") {
                        get("Point").shouldBeType(
                            TupleType(
                                listOf(
                                    Parameter("n").labeled("x"),
                                    Parameter("n").labeled("y")
                                )
                            ),
                            "(x: n, y: n)"
                        )
                    }
                    should("(x: Num n, y: Num n) type") {
                        get("Point2D").shouldBeType(
                            TupleType(
                                listOf(
                                    ParametricType(Concrete("Num"), listOf(Parameter("n"))).labeled("x"),
                                    ParametricType(Concrete("Num"), listOf(Parameter("n"))).labeled("y")
                                )
                            ),
                            "(x: Num n, y: Num n)"
                        )
                    }
                    should("(a: Int -> b -> result: Int) Type") {
                        get("Sum").shouldBeType(
                            FunctionType(
                                listOf(
                                    Concrete("Int").labeled("a"),
                                    Parameter("b"),
                                    Concrete("Int").labeled("result")
                                )
                            ),
                            "(a: Int -> b -> result: Int)"
                        )
                    }
                    should("(Some value: v | None) type") {
                        get("Option").shouldBeType(
                            UnionType(
                                mapOf(
                                    "Some" to UnionType.ClassType(
                                        "Some",
                                        listOf(Parameter("v").labeled("value"))
                                    ),
                                    "None" to UnionType.ClassType("None", listOf())
                                )
                            ),
                            "Some value: v\n|None"
                        )
                    }
                }
                should("Should have 9 types") {
                    size.shouldBe(9)
                }
                should("Shouldn't have errors") {
                    errors.shouldBeEmpty()
                }
            }
        }
        context("Order declaration shouldn't affect builder") {
            typeSystem {
                alias("Abs") {
                    functionType {
                        type("Int")
                        type("Int")
                    }
                }
                type("Int")
            }.apply {
                context("Should contain the following types:") {
                    should("Int type") {
                        get("Int").shouldBeType(Concrete("Int"), "Int")
                    }
                    should("Int -> Int type") {
                        get("Abs").shouldBeType(
                            FunctionType(listOf(Concrete("Int"), Concrete("Int"))),
                            "(Int -> Int)"
                        )
                    }
                }
                should("Should contains 2 types") {
                    size.shouldBe(2)
                }
                should("Shouldn't have errors") {
                    errors.shouldBeEmpty()
                }
            }
        }
    }
})