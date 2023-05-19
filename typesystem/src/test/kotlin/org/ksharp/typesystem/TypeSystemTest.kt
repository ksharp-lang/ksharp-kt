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
                type(TypeVisibility.Public, "Int", annotations)
                alias(TypeVisibility.Public, "Integer", annotations) {
                    type("Int")
                }
                parametricType(TypeVisibility.Public, "List", annotations) {
                    parameter("a")
                }
            }.apply {
                context("Should contains the following types:") {
                    should("Int type") {
                        get("Int").shouldBeType(
                            Annotated(annotations, Concrete(TypeVisibility.Public, "Int")),
                            "@impure(lang=kotlin) Int"
                        )
                    }
                    should("Integer type") {
                        get("Integer").shouldBeType(
                            Annotated(annotations, Alias(TypeVisibility.Public, "Int")),
                            "@impure(lang=kotlin) Int"
                        )
                    }
                    should("List type") {
                        get("List").shouldBeType(
                            Annotated(
                                annotations,
                                ParametricType(
                                    TypeVisibility.Public,
                                    Alias(TypeVisibility.Public, "List"),
                                    listOf(Parameter(TypeVisibility.Public, "a"))
                                )
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
                type(TypeVisibility.Public, "Int")
                type(TypeVisibility.Public, "String")
                alias(TypeVisibility.Public, "Integer") {
                    type("Int")
                }
                parametricType(TypeVisibility.Public, "Map") {
                    parameter("a")
                    parameter("b")
                }
                alias(TypeVisibility.Public, "StringMap") {
                    parametricType("Map") {
                        type("String")
                        parameter("b")
                    }
                }
                alias(TypeVisibility.Public, "Sum") {
                    functionType {
                        type("Int")
                        type("Int")
                        type("Int")
                    }
                } //.map { it.representation }.shouldBeRight("(Int -> Int -> Int)")
            }.apply {
                context("Should contains the following types:") {
                    should("Int type") {
                        get("Int").shouldBeType(Concrete(TypeVisibility.Public, "Int"), "Int")
                    }
                    should("String type") {
                        get("String").shouldBeType(Concrete(TypeVisibility.Public, "String"), "String")
                    }
                    should("Integer alias type") {
                        get("Integer").shouldBeType(Alias(TypeVisibility.Public, "Int"), "Int")
                    }
                    should("(Map a b) parametric type") {
                        get("Map").shouldBeType(
                            ParametricType(
                                TypeVisibility.Public,
                                Alias(TypeVisibility.Public, "Map"),
                                listOf(
                                    Parameter(TypeVisibility.Public, "a"),
                                    Parameter(TypeVisibility.Public, "b")
                                )
                            ), "(Map a b)"
                        )
                    }
                    should("StringMap should be alias of (Map String b)") {
                        get("StringMap").shouldBeType(
                            ParametricType(
                                TypeVisibility.Public,
                                Alias(TypeVisibility.Public, "Map"),
                                listOf(
                                    Alias(TypeVisibility.Public, "String"),
                                    Parameter(TypeVisibility.Public, "b")
                                )
                            ), "(Map String b)"
                        )
                    }
                    should("Sum should be function of Int -> Int -> Int") {
                        get("Sum").shouldBeType(
                            FunctionType(
                                TypeVisibility.Public,
                                listOf(
                                    Alias(TypeVisibility.Public, "Int"),
                                    Alias(TypeVisibility.Public, "Int"),
                                    Alias(TypeVisibility.Public, "Int")
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
                type(TypeVisibility.Public, "Int")
                parametricType(TypeVisibility.Public, "Map") {
                    type("Int")
                    type("Int")
                }
            }.apply {
                should("Type exists so should create an alias type") {
                    value.alias("Map").shouldBeType(Alias(TypeVisibility.Internal, "Map"), "Map")
                }
                should("Resolve alias type should returns the parametric type") {
                    value(Alias(TypeVisibility.Public, "Map")).shouldBeType(
                        ParametricType(
                            TypeVisibility.Public,
                            Alias(TypeVisibility.Public, "Map"),
                            listOf(Alias(TypeVisibility.Public, "Int"), Alias(TypeVisibility.Public, "Int"))
                        ),
                        "(Map Int Int)"
                    )
                }
                context("Not alias types should resolve to themself") {
                    value(Concrete(TypeVisibility.Public, "Int")).shouldBeType(
                        Concrete(TypeVisibility.Public, "Int"),
                        "Int"
                    )
                }
                context("Resolve a Labeled alias") {
                    value(
                        Labeled(
                            "n",
                            Alias(TypeVisibility.Public, "Map")
                        )
                    ).shouldBeType(
                        Labeled(
                            "n",
                            ParametricType(
                                TypeVisibility.Public,
                                Alias(TypeVisibility.Public, "Map"),
                                listOf(Alias(TypeVisibility.Public, "Int"), Alias(TypeVisibility.Public, "Int"))
                            )
                        ),
                        "n: (Map Int Int)"
                    )
                }
                context("Resolve a Annotated alias") {
                    value(
                        Annotated(
                            listOf(Annotation("anno", mapOf("key" to "value"))),
                            Alias(TypeVisibility.Public, "Map")
                        )
                    ).shouldBeType(
                        Annotated(
                            listOf(Annotation("anno", mapOf("key" to "value"))),
                            ParametricType(
                                TypeVisibility.Public,
                                Alias(TypeVisibility.Public, "Map"),
                                listOf(Alias(TypeVisibility.Public, "Int"), Alias(TypeVisibility.Public, "Int"))
                            )
                        ),
                        "@anno(key=value) Map Int Int"
                    )
                }
            }
        }
        context("Parametric Types") {
            typeSystem {
                parametricType(TypeVisibility.Public, "Map") {
                    parameter("a")
                    parameter("b")
                }

                parametricType(TypeVisibility.Public, "Either") {
                    parameter("a")
                    parametricType("Either") {
                        parameter("a")
                        parameter("b")
                    }
                }

                alias(TypeVisibility.Public, "StringMap") {
                    parametricType("Map") {
                        parameter("a")
                    }
                }
            }.apply {
                context("Should contains the following types:") {
                    should("(Map a b) type") {
                        get("Map").shouldBeType(
                            ParametricType(
                                TypeVisibility.Public,
                                Alias(TypeVisibility.Public, "Map"),
                                listOf(Parameter(TypeVisibility.Public, "a"), Parameter(TypeVisibility.Public, "b"))
                            ),
                            "(Map a b)"
                        )
                    }
                    should("Recursive: (Either a (Either a b)) type") {
                        get("Either").shouldBeType(
                            ParametricType(
                                TypeVisibility.Public,
                                Alias(TypeVisibility.Public, "Either"),
                                listOf(
                                    Parameter(TypeVisibility.Public, "a"),
                                    ParametricType(
                                        TypeVisibility.Public,
                                        Alias(TypeVisibility.Public, "Either"),
                                        listOf(
                                            Parameter(TypeVisibility.Public, "a"),
                                            Parameter(TypeVisibility.Public, "b")
                                        )
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
                                    TypeVisibility.Public,
                                    Alias(TypeVisibility.Public, "Map"),
                                    listOf(
                                        Parameter(TypeVisibility.Public, "a"),
                                        Parameter(TypeVisibility.Public, "b")
                                    )
                                ),
                                "number" to 2,
                                "configuredType" to ParametricType(
                                    TypeVisibility.Public,
                                    Alias(TypeVisibility.Public, "Map"),
                                    listOf(
                                        Parameter(TypeVisibility.Public, "a")
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
                    shouldBe(Parameter(TypeVisibility.Internal, "@0"))
                    intermediate.shouldBeTrue()
                }
                newParameter().shouldBe(Parameter(TypeVisibility.Internal, "@1"))
            }
            context("Normal parameters aren't intermediate") {
                Parameter(TypeVisibility.Internal, "a").intermediate.shouldBeFalse()
            }
        }
        context("Function types") {
            typeSystem {
                type(TypeVisibility.Public, "Int")
                parametricType(TypeVisibility.Public, "List") {
                    parameter("a")
                }
                alias(TypeVisibility.Public, "Sum") {
                    functionType {
                        type("Int")
                    }
                }
                alias(TypeVisibility.Public, "Sum") {
                    functionType {
                    }
                }
                alias(TypeVisibility.Public, "Sum") {
                    functionType {
                        type("Int")
                        type("Int")
                        type("Int")
                    }
                }
                alias(TypeVisibility.Public, "Abs") {
                    functionType {
                        parameter("a")
                        type("Int")
                    }
                }
                alias(TypeVisibility.Public, "Get") {
                    functionType {
                        parametricType("List") {
                            type("Int")
                        }
                        type("Int")
                    }
                }
                alias(TypeVisibility.Public, "MapFn") {
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
                        get("Int").shouldBeType(Concrete(TypeVisibility.Public, "Int"), "Int")
                    }
                    should("(List a) type") {
                        get("List").shouldBeType(
                            ParametricType(
                                TypeVisibility.Public,
                                Alias(TypeVisibility.Public, "List"),
                                listOf(Parameter(TypeVisibility.Public, "a"))
                            ),
                            "(List a)"
                        )
                    }
                    should("(Int -> Int -> Int) type") {
                        get("Sum").shouldBeType(
                            FunctionType(
                                TypeVisibility.Public,
                                listOf(
                                    Alias(TypeVisibility.Public, "Int"),
                                    Alias(TypeVisibility.Public, "Int"),
                                    Alias(TypeVisibility.Public, "Int")
                                )
                            ),
                            "(Int -> Int -> Int)"
                        )
                    }
                    should("(a -> Int) type") {
                        get("Abs").shouldBeType(
                            FunctionType(
                                TypeVisibility.Public,
                                listOf(Parameter(TypeVisibility.Public, "a"), Alias(TypeVisibility.Public, "Int"))
                            ),
                            "(a -> Int)"
                        )
                    }
                    should("((List Int) -> Int) type") {
                        get("Get")
                            .shouldBeType(
                                FunctionType(
                                    TypeVisibility.Public,
                                    listOf(
                                        ParametricType(
                                            TypeVisibility.Public,
                                            Alias(TypeVisibility.Public, "List"),
                                            listOf(Alias(TypeVisibility.Public, "Int"))
                                        ),
                                        Alias(TypeVisibility.Public, "Int")
                                    )
                                ),
                                "((List Int) -> Int)"
                            )
                    }
                    should("((List a) -> (a -> b) -> (List b)) type") {
                        get("MapFn")
                            .shouldBeType(
                                FunctionType(
                                    TypeVisibility.Public,
                                    listOf(
                                        ParametricType(
                                            TypeVisibility.Public,
                                            Alias(TypeVisibility.Public, "List"),
                                            listOf(Parameter(TypeVisibility.Public, "a"))
                                        ),
                                        FunctionType(
                                            TypeVisibility.Public,
                                            listOf(
                                                Parameter(TypeVisibility.Public, "a"),
                                                Parameter(TypeVisibility.Public, "b")
                                            )
                                        ),
                                        ParametricType(
                                            TypeVisibility.Public,
                                            Alias(TypeVisibility.Public, "List"),
                                            listOf(Parameter(TypeVisibility.Public, "b"))
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
                type(TypeVisibility.Public, "String")
                parametricType(TypeVisibility.Public, "Num") {
                    parameter("a")
                }
                alias(TypeVisibility.Public, "User") {
                    tupleType {
                        type("String")
                        type("String")
                    }
                }
                alias(TypeVisibility.Public, "Point") {
                    tupleType {
                        parametricType("Num") {
                            parameter("x")
                        }
                        parametricType("Num") {
                            parameter("x")
                        }
                    }
                }
                alias(TypeVisibility.Public, "NestedTuple") {
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
                                TypeVisibility.Public,
                                listOf(Alias(TypeVisibility.Public, "String"), Alias(TypeVisibility.Public, "String"))
                            ),
                            "(String, String)"
                        )
                    }
                    should("Point ((Num x), (Num x)) type") {
                        get("Point").shouldBeType(
                            TupleType(
                                TypeVisibility.Public,
                                listOf(
                                    ParametricType(
                                        TypeVisibility.Public,
                                        Alias(TypeVisibility.Public, "Num"),
                                        listOf(Parameter(TypeVisibility.Public, "x"))
                                    ),
                                    ParametricType(
                                        TypeVisibility.Public,
                                        Alias(TypeVisibility.Public, "Num"),
                                        listOf(Parameter(TypeVisibility.Public, "x"))
                                    ),
                                )
                            ),
                            "((Num x), (Num x))"
                        )
                    }
                    should("NestedTuple ((Num x), point: (String, x)) type") {
                        get("NestedTuple").shouldBeType(
                            TupleType(
                                TypeVisibility.Public,
                                listOf(
                                    ParametricType(
                                        TypeVisibility.Public,
                                        Alias(TypeVisibility.Public, "Num"),
                                        listOf(Parameter(TypeVisibility.Public, "x"))
                                    ),
                                    Labeled(
                                        "point", TupleType(
                                            TypeVisibility.Public,
                                            listOf(
                                                Alias(TypeVisibility.Public, "String"),
                                                Parameter(TypeVisibility.Public, "x")
                                            )
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
                type(TypeVisibility.Public, "String")
                parametricType(TypeVisibility.Public, "Dict") {
                    type("String")
                    parameter("b")
                }
                alias(TypeVisibility.Public, "Either") {
                    unionType {
                        clazz("Left") {
                            parameter("a")
                        }
                        clazz("Right") {
                            parameter("b")
                        }
                    }
                }
                alias(TypeVisibility.Public, "Weekend") {
                    unionType {
                        clazz("Saturday")
                        clazz("Sunday")
                    }
                }
                alias(TypeVisibility.Public, "Bool") {
                    unionType {
                        clazz("True")
                        clazz("False")
                    }
                }
                alias(TypeVisibility.Public, "Bool2") {
                    unionType {
                        clazz("True")
                    }
                }
            }.apply {
                context("Should contains the following types:") {
                    should("String type") {
                        get("String").shouldBeType(Concrete(TypeVisibility.Public, "String"), "String")
                    }
                    should("(Dict String b) type") {
                        get("Dict").shouldBeType(
                            ParametricType(
                                TypeVisibility.Public,
                                Alias(TypeVisibility.Public, "Dict"),
                                listOf(Alias(TypeVisibility.Public, "String"), Parameter(TypeVisibility.Public, "b"))
                            ),
                            "(Dict String b)"
                        )
                    }
                    should("Left a|Right b type") {
                        get("Either").shouldBeType(
                            UnionType(
                                TypeVisibility.Public,
                                mapOf(
                                    "Left" to UnionType.ClassType(
                                        TypeVisibility.Public,
                                        "Left",
                                        listOf(Parameter(TypeVisibility.Public, "a"))
                                    ),
                                    "Right" to UnionType.ClassType(
                                        TypeVisibility.Public,
                                        "Right",
                                        listOf(Parameter(TypeVisibility.Public, "b"))
                                    )
                                )
                            ),
                            "Left a\n|Right b"
                        )
                    }
                    should("Saturday|Sunday type") {
                        get("Weekend").shouldBeType(
                            UnionType(
                                TypeVisibility.Public,
                                mapOf(
                                    "Saturday" to UnionType.ClassType(TypeVisibility.Public, "Saturday", listOf()),
                                    "Sunday" to UnionType.ClassType(TypeVisibility.Public, "Sunday", listOf())
                                )
                            ),
                            "Saturday\n|Sunday"
                        )
                    }
                    should("True|False type") {
                        get("Bool").shouldBeType(
                            UnionType(
                                TypeVisibility.Public,
                                mapOf(
                                    "True" to UnionType.ClassType(TypeVisibility.Public, "True", listOf()),
                                    "False" to UnionType.ClassType(TypeVisibility.Public, "False", listOf())
                                )
                            ),
                            "True\n|False"
                        )
                    }
                    should("True type") {
                        get("True").shouldBeType(
                            TypeConstructor(TypeVisibility.Public, "True", "Bool"),
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
                    value(TypeConstructor(TypeVisibility.Public, "True", "Bool"))
                        .shouldBeType(
                            UnionType(
                                TypeVisibility.Public,
                                mapOf(
                                    "True" to UnionType.ClassType(TypeVisibility.Public, "True", listOf()),
                                    "False" to UnionType.ClassType(TypeVisibility.Public, "False", listOf())
                                )
                            ),
                            "True\n|False"
                        )
                }
            }
        }
        context("Trait types") {
            typeSystem {
                trait(TypeVisibility.Public, "Num", "a") {
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
                trait(TypeVisibility.Public, "num", "a") {
                    method("(+)") {
                        parameter("a")
                        parameter("a")
                        parameter("a")
                    }
                }
                trait(TypeVisibility.Public, "Functor", "F") {
                    method("map") {
                        functionType {
                            parameter("a")
                            parameter("b")
                        }
                        parameter("b")
                    }
                }
                trait(TypeVisibility.Public, "Monad", "m") {
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
                                TypeVisibility.Public,
                                "Num", "a", mapOf(
                                    "(+)" to TraitType.MethodType(
                                        TypeVisibility.Public,
                                        "(+)",
                                        listOf(
                                            Parameter(TypeVisibility.Public, "a"),
                                            Parameter(TypeVisibility.Public, "a"),
                                            Parameter(TypeVisibility.Public, "a")
                                        )
                                    ),
                                    "(-)" to TraitType.MethodType(
                                        TypeVisibility.Public,
                                        "(-)",
                                        listOf(
                                            Parameter(TypeVisibility.Public, "a"),
                                            Parameter(TypeVisibility.Public, "a"),
                                            Parameter(TypeVisibility.Public, "a")
                                        )
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
                type(TypeVisibility.Public, "Int")
                type(TypeVisibility.Public, "Char")
                alias(TypeVisibility.Public, "OrdNum") {
                    intersectionType {
                        type("Num")
                        type("Ord")
                    }
                }
                trait(TypeVisibility.Public, "Num", "a") {
                    method("(+)") {
                        parameter("a")
                        parameter("a")
                        parameter("a")
                    }
                }
                trait(TypeVisibility.Public, "Ord", "a") {
                    method("(<=)") {
                        parameter("a")
                        parameter("a")
                    }
                }
                alias(TypeVisibility.Public, "Str") {
                    intersectionType {
                        type("Int")
                        type("Char")
                    }
                }
            }.apply {
                context("Should contains the following types:") {
                    should("OrdNum intersection type") {
                        get("OrdNum").shouldBeType(
                            IntersectionType(
                                TypeVisibility.Public,
                                listOf(Alias(TypeVisibility.Public, "Num"), Alias(TypeVisibility.Public, "Ord"))
                            ),
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
                type(TypeVisibility.Public, "int")
                type(TypeVisibility.Public, "Int")
                alias(TypeVisibility.Public, "PInt") {
                    parametricType("Int") {}
                }
                parametricType(TypeVisibility.Public, "Test") {}
                alias(TypeVisibility.Public, "Float") {
                    type("Double")
                }
                parametricType(TypeVisibility.Public, "Float") {
                    type("Double")
                }
                type(TypeVisibility.Public, "Int")
            }.apply {
                should("Should have Int type") {
                    get("Int").shouldBeType(Concrete(TypeVisibility.Public, "Int"), "Int")
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
                type(TypeVisibility.Public, "String")
                type(TypeVisibility.Public, "Int")
                parametricType(TypeVisibility.Public, "Num") {
                    parameter("x")
                }
                parametricType(TypeVisibility.Public, "Map") {
                    parameter("k", "key")
                    parameter("v", "value")
                }
                alias(TypeVisibility.Public, "User") {
                    tupleType {
                        type("String", "name")
                        type("String", "password")
                    }
                }
                alias(TypeVisibility.Public, "Point") {
                    tupleType {
                        parameter("n", "x")
                        parameter("n", "y")
                    }
                }
                alias(TypeVisibility.Public, "Point2D") {
                    tupleType {
                        parametricType("Num", "x") {
                            parameter("n")
                        }
                        parametricType("Num", "y") {
                            parameter("n")
                        }
                    }
                }
                alias(TypeVisibility.Public, "Sum") {
                    functionType {
                        type("Int", "a")
                        parameter("b")
                        type("Int", "result")
                    }
                }
                alias(TypeVisibility.Public, "Option") {
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
                        get("String").shouldBeType(Concrete(TypeVisibility.Public, "String"), "String")
                    }
                    should("Int type") {
                        get("Int").shouldBeType(Concrete(TypeVisibility.Public, "Int"), "Int")
                    }
                    should("(Num x) type") {
                        get("Num").shouldBeType(
                            ParametricType(
                                TypeVisibility.Public,
                                Alias(TypeVisibility.Public, "Num"),
                                listOf(Parameter(TypeVisibility.Public, "x"))
                            ), "(Num x)"
                        )
                    }
                    should("(Map key: k value: v) type") {
                        get("Map").shouldBeType(
                            ParametricType(
                                TypeVisibility.Public,
                                Alias(TypeVisibility.Public, "Map"),
                                listOf(
                                    Parameter(TypeVisibility.Public, "k").labeled("key"),
                                    Parameter(TypeVisibility.Public, "v").labeled("value")
                                )
                            ), "(Map key: k value: v)"
                        )
                    }
                    should("(name: String, password: String) type") {
                        get("User").shouldBeType(
                            TupleType(
                                TypeVisibility.Public,
                                listOf(
                                    Alias(TypeVisibility.Public, "String").labeled("name"),
                                    Alias(TypeVisibility.Public, "String").labeled("password")
                                )
                            ),
                            "(name: String, password: String)"
                        )
                    }
                    should("(x: n, y: n) type") {
                        get("Point").shouldBeType(
                            TupleType(
                                TypeVisibility.Public,
                                listOf(
                                    Parameter(TypeVisibility.Public, "n").labeled("x"),
                                    Parameter(TypeVisibility.Public, "n").labeled("y")
                                )
                            ),
                            "(x: n, y: n)"
                        )
                    }
                    should("(x: Num n, y: Num n) type") {
                        get("Point2D").shouldBeType(
                            TupleType(
                                TypeVisibility.Public,
                                listOf(
                                    ParametricType(
                                        TypeVisibility.Public,
                                        Alias(TypeVisibility.Public, "Num"),
                                        listOf(Parameter(TypeVisibility.Public, "n"))
                                    ).labeled("x"),
                                    ParametricType(
                                        TypeVisibility.Public,
                                        Alias(TypeVisibility.Public, "Num"),
                                        listOf(Parameter(TypeVisibility.Public, "n"))
                                    ).labeled("y")
                                )
                            ),
                            "(x: (Num n), y: (Num n))"
                        )
                    }
                    should("(a: Int -> b -> result: Int) Type") {
                        get("Sum").shouldBeType(
                            FunctionType(
                                TypeVisibility.Public,
                                listOf(
                                    Alias(TypeVisibility.Public, "Int").labeled("a"),
                                    Parameter(TypeVisibility.Public, "b"),
                                    Alias(TypeVisibility.Public, "Int").labeled("result")
                                )
                            ),
                            "(a: Int -> b -> result: Int)"
                        )
                    }
                    should("(Some value: v | None) type") {
                        get("Option").shouldBeType(
                            UnionType(
                                TypeVisibility.Public,
                                mapOf(
                                    "Some" to UnionType.ClassType(
                                        TypeVisibility.Public,
                                        "Some",
                                        listOf(Parameter(TypeVisibility.Public, "v").labeled("value"))
                                    ),
                                    "None" to UnionType.ClassType(TypeVisibility.Public, "None", listOf())
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
                alias(TypeVisibility.Public, "Abs") {
                    functionType {
                        type("Int")
                        type("Int")
                    }
                }
                type(TypeVisibility.Public, "Int")
            }.apply {
                context("Should contain the following types:") {
                    should("Int type") {
                        get("Int").shouldBeType(Concrete(TypeVisibility.Public, "Int"), "Int")
                    }
                    should("Int -> Int type") {
                        get("Abs").shouldBeType(
                            FunctionType(
                                TypeVisibility.Public,
                                listOf(Alias(TypeVisibility.Public, "Int"), Alias(TypeVisibility.Public, "Int"))
                            ),
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
                type(TypeVisibility.Public, "Int")
            }) {
                parametricType(TypeVisibility.Public, "List") {
                    type("Int")
                }
            }.apply {
                should("No have errors") {
                    errors.shouldBeEmpty()
                }
                should("Can find Int") {
                    get("Int").shouldBeType(Concrete(TypeVisibility.Public, "Int"), "Int")
                }
                should("Can find List Int") {
                    get("List").shouldBeType(
                        ParametricType(
                            TypeVisibility.Public,
                            Alias(TypeVisibility.Public, "List"),
                            listOf(Alias(TypeVisibility.Public, "Int"))
                        ), "(List Int)"
                    )
                }
            }
        }
        context("Hierarchical type system with Errors") {
            typeSystem(typeSystem {
                type(TypeVisibility.Public, "Int")
                parametricType(TypeVisibility.Public, "String") {
                    type("Array")
                }
            }) {
                type(TypeVisibility.Public, "Int")
                parametricType(TypeVisibility.Public, "Set") {
                    type("String")
                }
                parametricType(TypeVisibility.Public, "List") {
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
                    get("Int").shouldBeType(Concrete(TypeVisibility.Public, "Int"), "Int")
                }
                should("Can find List Int") {
                    get("List").shouldBeType(
                        ParametricType(
                            TypeVisibility.Public,
                            Alias(TypeVisibility.Public, "List"),
                            listOf(Alias(TypeVisibility.Public, "Int"))
                        ), "(List Int)"
                    )
                }
            }
        }
        context("Module type system") {
            moduleTypeSystem {
                register("num", typeSystem {
                    type(TypeVisibility.Public, "Int")
                    type(TypeVisibility.Public, "Float")
                })
                register("str", typeSystem {
                    type(TypeVisibility.Public, "String")
                })
            }.apply {
                errors.shouldBeEmpty()
                size.shouldBe(0)
                get("num.Int").shouldBeType(Concrete(TypeVisibility.Public, "Int"), "Int")
            }
        }
        context("Module type system with parent") {
            moduleTypeSystem(typeSystem {
                type(TypeVisibility.Public, "List")
            }) {
                register("num", typeSystem {
                    type(TypeVisibility.Public, "Int")
                    type(TypeVisibility.Public, "Float")
                })
                register("str", typeSystem {
                    type(TypeVisibility.Public, "String")
                })
            }.apply {
                errors.shouldBeEmpty()
                size.shouldBe(0)
                get("List").shouldBeType(Concrete(TypeVisibility.Public, "List"), "List")
                get("num.Int").shouldBeType(Concrete(TypeVisibility.Public, "Int"), "Int")
            }
        }
        context("Module type system with parent and errors") {
            moduleTypeSystem(typeSystem {
                type(TypeVisibility.Public, "List")
                type(TypeVisibility.Public, "List")
            }) {
                register("num", typeSystem {
                    type(TypeVisibility.Public, "Int")
                    type(TypeVisibility.Public, "Int")
                    type(TypeVisibility.Public, "Float")
                })
                register("str", typeSystem {
                    type(TypeVisibility.Public, "String")
                })
            }.apply {
                errors.shouldBe(
                    listOf(
                        TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to "List"),
                        TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to "Int"),
                    )
                )
                size.shouldBe(0)
                get("List").shouldBeType(Concrete(TypeVisibility.Public, "List"), "List")
                get("num.Int").shouldBeType(Concrete(TypeVisibility.Public, "Int"), "Int")
                get("BigInt").shouldBeLeft()
                get("num.BigDec").shouldBeLeft()
                get("txt.String").shouldBeLeft()
            }
        }
        context("Check list all types on the typeSystem") {
            typeSystem {
                type(TypeVisibility.Public, "Int")
                type(TypeVisibility.Public, "String")
            }.apply {
                value.asSequence()
                    .toList()
                    .shouldBe(
                        listOf(
                            "Int" to Concrete(TypeVisibility.Public, "Int"),
                            "String" to Concrete(TypeVisibility.Public, "String")
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