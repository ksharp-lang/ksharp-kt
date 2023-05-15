package org.ksharp.typesystem

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.common.new
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.annotations.Annotation
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
                        get("Integer").shouldBeType(Annotated(annotations, Alias("Int")), "@impure(lang=kotlin) Int")
                    }
                    should("List type") {
                        get("List").shouldBeType(
                            Annotated(
                                annotations,
                                ParametricType(Alias("List"), listOf(Parameter("a")))
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
                        get("Integer").shouldBeType(Alias("Int"), "Int")
                    }
                    should("(Map a b) parametric type") {
                        get("Map").shouldBeType(
                            ParametricType(
                                Alias("Map"),
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
                                Alias("Map"),
                                listOf(
                                    Alias("String"),
                                    Parameter("b")
                                )
                            ), "(Map String b)"
                        )
                    }
                    should("Sum should be function of Int -> Int -> Int") {
                        get("Sum").shouldBeType(
                            FunctionType(
                                listOf(
                                    Alias("Int"),
                                    Alias("Int"),
                                    Alias("Int")
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
        context("Get alias type and resolve the type against the typeSystem") {
            typeSystem {
                type("Int")
                parametricType("Map") {
                    type("Int")
                    type("Int")
                }
            }.apply {
                should("Type exists so should create an alias type") {
                    value.alias("Map").shouldBeType(Alias("Map"), "Map")
                }
                should("Resolve alias type should returns the parametric type") {
                    value(Alias("Map")).shouldBeType(
                        ParametricType(Alias("Map"), listOf(Alias("Int"), Alias("Int"))),
                        "(Map Int Int)"
                    )
                }
                context("Not alias types should resolve to themself") {
                    value(Concrete("Int")).shouldBeType(
                        Concrete("Int"),
                        "Int"
                    )
                }
                context("Resolve a Labeled alias") {
                    value(
                        Labeled(
                            "n",
                            Alias("Map")
                        )
                    ).shouldBeType(
                        Labeled("n", ParametricType(Alias("Map"), listOf(Alias("Int"), Alias("Int")))),
                        "n: (Map Int Int)"
                    )
                }
                context("Resolve a Annotated alias") {
                    value(
                        Annotated(
                            listOf(Annotation("anno", mapOf("key" to "value"))),
                            Alias("Map")
                        )
                    ).shouldBeType(
                        Annotated(
                            listOf(Annotation("anno", mapOf("key" to "value"))),
                            ParametricType(Alias("Map"), listOf(Alias("Int"), Alias("Int")))
                        ),
                        "@anno(key=value) Map Int Int"
                    )
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
                                Alias("Map"),
                                listOf(Parameter("a"), Parameter("b"))
                            ),
                            "(Map a b)"
                        )
                    }
                    should("Recursive: (Either a (Either a b)) type") {
                        get("Either").shouldBeType(
                            ParametricType(
                                Alias("Either"),
                                listOf(
                                    Parameter("a"),
                                    ParametricType(
                                        Alias("Either"),
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
                                    Alias("Map"),
                                    listOf(
                                        Parameter("a"),
                                        Parameter("b")
                                    )
                                ),
                                "number" to 2,
                                "configuredType" to ParametricType(
                                    Alias("Map"),
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
        context("Parameters") {
            context("Intermediate") {
                newParameter().apply {
                    shouldBe(Parameter("@0"))
                    intermediate.shouldBeTrue()
                }
                newParameter().shouldBe(Parameter("@1"))
            }
            context("Normal parameters aren't intermediate") {
                Parameter("a").intermediate.shouldBeFalse()
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
                                Alias("List"),
                                listOf(Parameter("a"))
                            ),
                            "(List a)"
                        )
                    }
                    should("(Int -> Int -> Int) type") {
                        get("Sum").shouldBeType(
                            FunctionType(
                                listOf(Alias("Int"), Alias("Int"), Alias("Int"))
                            ),
                            "(Int -> Int -> Int)"
                        )
                    }
                    should("(a -> Int) type") {
                        get("Abs").shouldBeType(
                            FunctionType(listOf(Parameter("a"), Alias("Int"))),
                            "(a -> Int)"
                        )
                    }
                    should("((List Int) -> Int) type") {
                        get("Get")
                            .shouldBeType(
                                FunctionType(
                                    listOf(
                                        ParametricType(
                                            Alias("List"),
                                            listOf(Alias("Int"))
                                        ),
                                        Alias("Int")
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
                                            Alias("List"),
                                            listOf(Parameter("a"))
                                        ),
                                        FunctionType(
                                            listOf(
                                                Parameter("a"),
                                                Parameter("b")
                                            )
                                        ),
                                        ParametricType(
                                            Alias("List"),
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
                alias("NestedTuple") {
                    tupleType {
                        parametricType("Num") {
                            parameter("x")
                        }
                        tupleType("point") {
                            type("String")
                            parameter("x")
                        }
                    }
                }
            }.apply {
                context("Should contains the following types:") {
                    should("User (String, String) type") {
                        get("User").shouldBeType(
                            TupleType(
                                listOf(Alias("String"), Alias("String"))
                            ),
                            "(String, String)"
                        )
                    }
                    should("Point ((Num x), (Num x)) type") {
                        get("Point").shouldBeType(
                            TupleType(
                                listOf(
                                    ParametricType(
                                        Alias("Num"), listOf(Parameter("x"))
                                    ),
                                    ParametricType(
                                        Alias("Num"), listOf(Parameter("x"))
                                    ),
                                )
                            ),
                            "((Num x), (Num x))"
                        )
                    }
                    should("NestedTuple ((Num x), point: (String, x)) type") {
                        get("NestedTuple").shouldBeType(
                            TupleType(
                                listOf(
                                    ParametricType(
                                        Alias("Num"), listOf(Parameter("x"))
                                    ),
                                    Labeled(
                                        "point", TupleType(
                                            listOf(Alias("String"), Parameter("x"))
                                        )
                                    ),
                                )
                            ),
                            "((Num x), point: (String, x))"
                        )
                    }
                }
                should("Should have 4 types") {
                    size.shouldBe(5)
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
                alias("Bool") {
                    unionType {
                        clazz("True")
                        clazz("False")
                    }
                }
                alias("Bool2") {
                    unionType {
                        clazz("True")
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
                                Alias("Dict"),
                                listOf(Alias("String"), Parameter("b"))
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
                    should("True|False type") {
                        get("Bool").shouldBeType(
                            UnionType(
                                mapOf(
                                    "True" to UnionType.ClassType("True", listOf()),
                                    "False" to UnionType.ClassType("False", listOf())
                                )
                            ),
                            "True\n|False"
                        )
                    }
                    should("True type") {
                        get("True").shouldBeType(
                            TypeConstructor("True", "Bool"),
                            "True"
                        )
                    }
                }
                should("Should have 7 types") {
                    size.shouldBe(11)
                }
                should("Should have already registered errors") {
                    errors.shouldBe(
                        listOf(
                            TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to "True")
                        )
                    )
                }
                should("Resolve type constructor types") {
                    value(TypeConstructor("True", "Bool"))
                        .shouldBeType(
                            UnionType(
                                mapOf(
                                    "True" to UnionType.ClassType("True", listOf()),
                                    "False" to UnionType.ClassType("False", listOf())
                                )
                            ),
                            "True\n|False"
                        )
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
                            IntersectionType(listOf(Alias("Num"), Alias("Ord"))),
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
                        get("Num").shouldBeType(ParametricType(Alias("Num"), listOf(Parameter("x"))), "(Num x)")
                    }
                    should("(Map key: k value: v) type") {
                        get("Map").shouldBeType(
                            ParametricType(
                                Alias("Map"),
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
                                    Alias("String").labeled("name"),
                                    Alias("String").labeled("password")
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
                                    ParametricType(Alias("Num"), listOf(Parameter("n"))).labeled("x"),
                                    ParametricType(Alias("Num"), listOf(Parameter("n"))).labeled("y")
                                )
                            ),
                            "(x: (Num n), y: (Num n))"
                        )
                    }
                    should("(a: Int -> b -> result: Int) Type") {
                        get("Sum").shouldBeType(
                            FunctionType(
                                listOf(
                                    Alias("Int").labeled("a"),
                                    Parameter("b"),
                                    Alias("Int").labeled("result")
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
                should("Should have 11 types") {
                    size.shouldBe(11)
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
                            FunctionType(listOf(Alias("Int"), Alias("Int"))),
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
        context("Hierarchical type system") {
            typeSystem(typeSystem {
                type("Int")
            }) {
                parametricType("List") {
                    type("Int")
                }
            }.apply {
                should("No have errors") {
                    errors.shouldBeEmpty()
                }
                should("Can find Int") {
                    get("Int").shouldBeType(Concrete("Int"), "Int")
                }
                should("Can find List Int") {
                    get("List").shouldBeType(ParametricType(Alias("List"), listOf(Alias("Int"))), "(List Int)")
                }
            }
        }
        context("Hierarchical type system with Errors") {
            typeSystem(typeSystem {
                type("Int")
                parametricType("String") {
                    type("Array")
                }
            }) {
                type("Int")
                parametricType("Set") {
                    type("String")
                }
                parametricType("List") {
                    type("Int")
                }
            }.apply {
                should("Should have one errors") {
                    errors.shouldBe(
                        listOf(
                            TypeSystemErrorCode.TypeNotFound.new("type" to "Array"),
                            TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to "Int"),
                            TypeSystemErrorCode.TypeNotFound.new("type" to "String"),
                        )
                    )
                }
                should("Can find Int") {
                    get("Int").shouldBeType(Concrete("Int"), "Int")
                }
                should("Can find List Int") {
                    get("List").shouldBeType(ParametricType(Alias("List"), listOf(Alias("Int"))), "(List Int)")
                }
            }
        }
        context("Module type system") {
            moduleTypeSystem {
                register("num", typeSystem {
                    type("Int")
                    type("Float")
                })
                register("str", typeSystem {
                    type("String")
                })
            }.apply {
                errors.shouldBeEmpty()
                size.shouldBe(0)
                get("num.Int").shouldBeType(Concrete("Int"), "Int")
            }
        }
        context("Module type system with parent") {
            moduleTypeSystem(typeSystem {
                type("List")
            }) {
                register("num", typeSystem {
                    type("Int")
                    type("Float")
                })
                register("str", typeSystem {
                    type("String")
                })
            }.apply {
                errors.shouldBeEmpty()
                size.shouldBe(0)
                get("List").shouldBeType(Concrete("List"), "List")
                get("num.Int").shouldBeType(Concrete("Int"), "Int")
            }
        }
        context("Module type system with parent and errors") {
            moduleTypeSystem(typeSystem {
                type("List")
                type("List")
            }) {
                register("num", typeSystem {
                    type("Int")
                    type("Int")
                    type("Float")
                })
                register("str", typeSystem {
                    type("String")
                })
            }.apply {
                errors.shouldBe(
                    listOf(
                        TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to "List"),
                        TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to "Int"),
                    )
                )
                size.shouldBe(0)
                get("List").shouldBeType(Concrete("List"), "List")
                get("num.Int").shouldBeType(Concrete("Int"), "Int")
                get("BigInt").shouldBeLeft()
                get("num.BigDec").shouldBeLeft()
                get("txt.String").shouldBeLeft()
            }
        }
        context("Check list all types on the typeSystem") {
            typeSystem {
                type("Int")
                type("String")
            }.apply {
                value.asSequence()
                    .toList()
                    .shouldBe(
                        listOf(
                            "Int" to Concrete("Int"),
                            "String" to Concrete("String")
                        )
                    )
            }
        }
    }
}) {
    override suspend fun beforeAny(testCase: TestCase) {
        super.beforeAny(testCase)
        resetParameterCounterForTesting()
    }

}