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
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.*

fun ErrorOrType.shouldBeType(type: Type, repr: String) =
    this.apply {
        map { it.representation }.shouldBeRight(repr)
        shouldBeRight(type)
    }

class TypeSystemTest : ShouldSpec({
    context("Given a type system. Check:") {
        context("Concrete, Aliases, Parametric and Function Types") {
            typeSystem {
                type(setOf(CommonAttribute.Public), "Int")
                type(setOf(CommonAttribute.Public), "String")
                type(setOf(CommonAttribute.Internal), "Integer") {
                    alias("Int")
                }
                parametricType(setOf(CommonAttribute.Public), "Map") {
                    parameter("a")
                    parameter("b")
                }
                type(NoAttributes, "StringMap") {
                    parametricType("Map") {
                        type("String")
                        parameter("b")
                    }
                }
                type(NoAttributes, "Sum") {
                    functionType {
                        type("Int")
                        type("Int")
                        type("Int")
                    }
                } //.map { it.representation }.shouldBeRight("(Int -> Int -> Int)")
            }.apply {
                context("Should contains the following types:") {
                    should("Int type") {
                        get("Int").shouldBeType(Concrete(setOf(CommonAttribute.Public), "Int"), "Int")
                    }
                    should("String type") {
                        get("String").shouldBeType(Concrete(setOf(CommonAttribute.Public), "String"), "String")
                    }
                    should("Integer alias type") {
                        get("Integer").shouldBeType(TypeAlias(setOf(CommonAttribute.Internal), "Int"), "Int")
                    }
                    should("(Map a b) parametric type") {
                        get("Map").shouldBeType(
                            ParametricType(
                                setOf(CommonAttribute.Public),
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
                                NoAttributes,
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
                                NoAttributes,
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
                type(setOf(CommonAttribute.Public), "Int")
                parametricType(setOf(CommonAttribute.Public), "Map") {
                    type("Int")
                    type("Int")
                }
            }.apply {
                should("Type exists so should create an alias type") {
                    value.alias(name = "Map").shouldBeType(Alias("Map"), "Map")
                }
                should("Resolve alias type should returns the parametric type") {
                    value(Alias("Map")).shouldBeType(
                        ParametricType(
                            setOf(CommonAttribute.Public),
                            Alias("Map"),
                            listOf(
                                Alias("Int"),
                                Alias("Int")
                            )
                        ),
                        "(Map Int Int)"
                    )
                }
                context("Not alias types should resolve to themself") {
                    value(Concrete(setOf(CommonAttribute.Public), "Int")).shouldBeType(
                        Concrete(setOf(CommonAttribute.Public), "Int"),
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
                        Labeled(
                            "n",
                            ParametricType(
                                setOf(CommonAttribute.Public),
                                Alias("Map"),
                                listOf(
                                    Alias("Int"),
                                    Alias("Int")
                                )
                            )
                        ),
                        "n: (Map Int Int)"
                    )
                }
            }
        }
        context("Parametric Types") {
            typeSystem {
                parametricType(setOf(CommonAttribute.Public), "Map") {
                    parameter("a")
                    parameter("b")
                }

                parametricType(setOf(CommonAttribute.Public), "Either") {
                    parameter("a")
                    parametricType("Either") {
                        parameter("a")
                        parameter("b")
                    }
                }

                type(setOf(CommonAttribute.Pure), "EitherAlias") {
                    alias("Either")
                }
                type(NoAttributes, "StringMap") {
                    parametricType("Map") {
                        parameter("a")
                    }
                }
            }.apply {
                context("Should contains the following types:") {
                    should("(Map a b) type") {
                        get("Map").shouldBeType(
                            ParametricType(
                                setOf(CommonAttribute.Public),
                                Alias("Map"),
                                listOf(
                                    Parameter("a"),
                                    Parameter("b")
                                )
                            ),
                            "(Map a b)"
                        )
                    }
                    should("Recursive: (Either a (Either a b)) type") {
                        get("Either").shouldBeType(
                            ParametricType(
                                setOf(CommonAttribute.Public),
                                Alias("Either"),
                                listOf(
                                    Parameter("a"),
                                    ParametricType(
                                        NoAttributes,
                                        Alias("Either"),
                                        listOf(
                                            Parameter("a"),
                                            Parameter("b")
                                        )
                                    )
                                )
                            ),
                            "(Either a (Either a b))"
                        )
                    }
                    should("Recursive: (Either a (Either a b)) alias type") {
                        invoke(get("EitherAlias").valueOrNull!!).shouldBeType(
                            ParametricType(
                                setOf(CommonAttribute.Public, CommonAttribute.Pure),
                                Alias("Either"),
                                listOf(
                                    Parameter("a"),
                                    ParametricType(
                                        NoAttributes,
                                        Alias("Either"),
                                        listOf(
                                            Parameter("a"),
                                            Parameter("b")
                                        )
                                    )
                                )
                            ),
                            "(Either a (Either a b))"
                        )
                    }
                }
                should("Should contain 3 types") {
                    size.shouldBe(3)
                }
                should("Should have errors") {
                    errors.shouldBe(
                        listOf(
                            TypeSystemErrorCode.InvalidNumberOfParameters.new(
                                "type" to ParametricType(
                                    setOf(CommonAttribute.Public),
                                    Alias("Map"),
                                    listOf(
                                        Parameter("a"),
                                        Parameter("b")
                                    )
                                ),
                                "number" to 2,
                                "configuredType" to ParametricType(
                                    NoAttributes,
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
                type(setOf(CommonAttribute.Public), "Int")
                parametricType(setOf(CommonAttribute.Public), "List") {
                    parameter("a")
                }
                type(NoAttributes, "Sum") {
                    functionType {
                        type("Int")
                    }
                }
                type(NoAttributes, "Sum") {
                    functionType {
                    }
                }
                type(setOf(CommonAttribute.Public), "Sum") {
                    functionType {
                        type("Int")
                        type("Int")
                        type("Int")
                    }
                }
                type(setOf(CommonAttribute.Public), "Abs") {
                    functionType {
                        parameter("a")
                        type("Int")
                    }
                }
                type(setOf(CommonAttribute.Public), "Get") {
                    functionType {
                        parametricType("List") {
                            type("Int")
                        }
                        type("Int")
                    }
                }
                type(setOf(CommonAttribute.Public), "MapFn") {
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
                type(setOf(CommonAttribute.Native), "SumAlias") {
                    alias("Sum")
                }
            }.apply {
                context("Should contains the following types:") {
                    should("Int type") {
                        get("Int").shouldBeType(Concrete(setOf(CommonAttribute.Public), "Int"), "Int")
                    }
                    should("(List a) type") {
                        get("List").shouldBeType(
                            ParametricType(
                                setOf(CommonAttribute.Public),
                                Alias("List"),
                                listOf(Parameter("a"))
                            ),
                            "(List a)"
                        )
                    }
                    should("(Int -> Int -> Int) type") {
                        get("Sum").shouldBeType(
                            FunctionType(
                                setOf(CommonAttribute.Public),
                                listOf(
                                    Alias("Int"),
                                    Alias("Int"),
                                    Alias("Int")
                                )
                            ),
                            "(Int -> Int -> Int)"
                        )
                    }
                    should("(a -> Int) type") {
                        get("Abs").shouldBeType(
                            FunctionType(
                                setOf(CommonAttribute.Public),
                                listOf(
                                    Parameter("a"),
                                    Alias("Int")
                                )
                            ),
                            "(a -> Int)"
                        )
                    }
                    should("((List Int) -> Int) type") {
                        get("Get")
                            .shouldBeType(
                                FunctionType(
                                    setOf(CommonAttribute.Public),
                                    listOf(
                                        ParametricType(
                                            NoAttributes,
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
                                    setOf(CommonAttribute.Public),
                                    listOf(
                                        ParametricType(
                                            NoAttributes,
                                            Alias("List"),
                                            listOf(Parameter("a"))
                                        ),
                                        FunctionType(
                                            NoAttributes,
                                            listOf(
                                                Parameter("a"),
                                                Parameter("b")
                                            )
                                        ),
                                        ParametricType(
                                            NoAttributes,
                                            Alias("List"),
                                            listOf(Parameter("b"))
                                        ),
                                    )
                                ),
                                "((List a) -> (a -> b) -> (List b))"
                            )
                    }
                    should("SumAlias (Int -> Int -> Int) type") {
                        invoke(get("SumAlias").valueOrNull!!).shouldBeType(
                            FunctionType(
                                setOf(CommonAttribute.Public, CommonAttribute.Native),
                                listOf(
                                    Alias("Int"),
                                    Alias("Int"),
                                    Alias("Int")
                                )
                            ),
                            "(Int -> Int -> Int)"
                        )
                    }
                }
                should("Should have 7 types") {
                    size.shouldBe(7)
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
                type(setOf(CommonAttribute.Public), "String")
                parametricType(setOf(CommonAttribute.Public), "Num") {
                    parameter("a")
                }
                type(setOf(CommonAttribute.Public), "User") {
                    tupleType {
                        type("String")
                        type("String")
                    }
                }
                type(setOf(CommonAttribute.Public), "Point") {
                    tupleType {
                        parametricType("Num") {
                            parameter("x")
                        }
                        parametricType("Num") {
                            parameter("x")
                        }
                    }
                }
                type(setOf(CommonAttribute.Native), "FloatPoint") {
                    alias("Point")
                }
                type(setOf(CommonAttribute.Public), "NestedTuple") {
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
                                setOf(CommonAttribute.Public),
                                listOf(
                                    Alias("String"),
                                    Alias("String")
                                )
                            ),
                            "(String, String)"
                        )
                    }
                    should("Point ((Num x), (Num x)) type") {
                        get("Point").shouldBeType(
                            TupleType(
                                setOf(CommonAttribute.Public),
                                listOf(
                                    ParametricType(
                                        NoAttributes,
                                        Alias("Num"),
                                        listOf(Parameter("x"))
                                    ),
                                    ParametricType(
                                        NoAttributes,
                                        Alias("Num"),
                                        listOf(Parameter("x"))
                                    ),
                                )
                            ),
                            "((Num x), (Num x))"
                        )
                    }
                    should("FloatPoint typealias ((Num x), (Num x)) type") {
                        invoke(get("FloatPoint").valueOrNull!!).shouldBeType(
                            TupleType(
                                setOf(CommonAttribute.Public, CommonAttribute.Native),
                                listOf(
                                    ParametricType(
                                        NoAttributes,
                                        Alias("Num"),
                                        listOf(Parameter("x"))
                                    ),
                                    ParametricType(
                                        NoAttributes,
                                        Alias("Num"),
                                        listOf(Parameter("x"))
                                    ),
                                )
                            ),
                            "((Num x), (Num x))"
                        )
                    }
                    should("NestedTuple ((Num x), point: (String, x)) type") {
                        get("NestedTuple").shouldBeType(
                            TupleType(
                                setOf(CommonAttribute.Public),
                                listOf(
                                    ParametricType(
                                        NoAttributes,
                                        Alias("Num"),
                                        listOf(Parameter("x"))
                                    ),
                                    Labeled(
                                        "point", TupleType(
                                            NoAttributes,
                                            listOf(
                                                Alias("String"),
                                                Parameter("x")
                                            )
                                        )
                                    ),
                                )
                            ),
                            "((Num x), point: (String, x))"
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
        context("Union types") {
            typeSystem {
                type(setOf(CommonAttribute.Public), "String")
                parametricType(setOf(CommonAttribute.Public), "Dict") {
                    type("String")
                    parameter("b")
                }
                type(setOf(CommonAttribute.Public), "Either") {
                    unionType {
                        clazz("Left") {
                            parameter("a")
                        }
                        clazz("Right") {
                            parameter("b")
                        }
                    }
                }
                type(setOf(CommonAttribute.Public), "Weekend") {
                    unionType {
                        clazz("Saturday")
                        clazz("Sunday")
                    }
                }
                type(setOf(CommonAttribute.Public), "Bool") {
                    unionType {
                        clazz("True")
                        clazz("False")
                    }
                }
                type(setOf(CommonAttribute.Constant), "Boolean") {
                    alias("Bool")
                }
                type(NoAttributes, "Bool2") {
                    unionType {
                        clazz("True")
                    }
                }
            }.apply {
                context("Should contains the following types:") {
                    should("String type") {
                        get("String").shouldBeType(Concrete(setOf(CommonAttribute.Public), "String"), "String")
                    }
                    should("(Dict String b) type") {
                        get("Dict").shouldBeType(
                            ParametricType(
                                setOf(CommonAttribute.Public),
                                Alias("Dict"),
                                listOf(
                                    Alias("String"),
                                    Parameter("b")
                                )
                            ),
                            "(Dict String b)"
                        )
                    }
                    should("Left a|Right b type") {
                        get("Either").shouldBeType(
                            UnionType(
                                setOf(CommonAttribute.Public),
                                mapOf(
                                    "Left" to UnionType.ClassType(
                                        "Left",
                                        listOf(Parameter("a"))
                                    ),
                                    "Right" to UnionType.ClassType(
                                        "Right",
                                        listOf(Parameter("b"))
                                    )
                                )
                            ),
                            "Left a\n|Right b"
                        )
                    }
                    should("Saturday|Sunday type") {
                        get("Weekend").shouldBeType(
                            UnionType(
                                setOf(CommonAttribute.Public),
                                mapOf(
                                    "Saturday" to UnionType.ClassType(
                                        "Saturday",
                                        listOf()
                                    ),
                                    "Sunday" to UnionType.ClassType("Sunday", listOf())
                                )
                            ),
                            "Saturday\n|Sunday"
                        )
                    }
                    should("True|False type") {
                        get("Bool").shouldBeType(
                            UnionType(
                                setOf(CommonAttribute.Public),
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
                            TypeConstructor(setOf(CommonAttribute.Public), "True", "Bool"),
                            "True"
                        )
                    }
                    should("Resolve type alias Boolean") {
                        invoke(get("Boolean").valueOrNull!!).shouldBeType(
                            UnionType(
                                setOf(CommonAttribute.Public, CommonAttribute.Constant),
                                mapOf(
                                    "True" to UnionType.ClassType("True", listOf()),
                                    "False" to UnionType.ClassType("False", listOf())
                                )
                            ),
                            "True\n|False"
                        )
                    }
                }
                should("Should have 12 types") {
                    size.shouldBe(12)
                }
                should("Should have already registered errors") {
                    errors.shouldBe(
                        listOf(
                            TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to "True")
                        )
                    )
                }
                should("Resolve type constructor types") {
                    value(TypeConstructor(setOf(CommonAttribute.Public), "True", "Bool"))
                        .shouldBeType(
                            UnionType(
                                setOf(CommonAttribute.Public),
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
                trait(setOf(CommonAttribute.Public), "Num", "a") {
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
                trait(setOf(CommonAttribute.Public), "Num2", "a") {
                    method("(+)") {
                        parameter("a")
                        parameter("a")
                        parameter("a")
                    }
                    method("(+)") {
                        parameter("a")
                        parameter("a")
                        parameter("a")
                    }
                }
                trait(setOf(CommonAttribute.Public), "num", "a") {
                    method("(+)") {
                        parameter("a")
                        parameter("a")
                        parameter("a")
                    }
                }
                trait(setOf(CommonAttribute.Public), "Functor", "F") {
                    method("map") {
                        functionType {
                            parameter("a")
                            parameter("b")
                        }
                        parameter("b")
                    }
                }
                trait(setOf(CommonAttribute.Public), "Monad", "m") {
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
                                setOf(CommonAttribute.Public),
                                "Num", "a", mapOf(
                                    "(+)/3" to TraitType.MethodType(
                                        setOf(CommonAttribute.Public),
                                        "(+)",
                                        listOf(
                                            Parameter("a"),
                                            Parameter("a"),
                                            Parameter("a")
                                        )
                                    ),
                                    "(-)/3" to TraitType.MethodType(
                                        setOf(CommonAttribute.Public),
                                        "(-)",
                                        listOf(
                                            Parameter("a"),
                                            Parameter("a"),
                                            Parameter("a")
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
                            TypeSystemErrorCode.DuplicateTraitMethod.new("name" to "(+)/3", "trait" to "Num2"),
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
                type(setOf(CommonAttribute.Public), "Int")
                type(setOf(CommonAttribute.Public), "Char")
                type(setOf(CommonAttribute.Internal), "OrdNum") {
                    intersectionType {
                        type("Num")
                        type("Ord")
                    }
                }
                type(NoAttributes, "TypeOrdNum") {
                    alias("OrdNum")
                }
                trait(setOf(CommonAttribute.Public), "Num", "a") {
                    method("(+)") {
                        parameter("a")
                        parameter("a")
                        parameter("a")
                    }
                }
                trait(setOf(CommonAttribute.Public), "Ord", "a") {
                    method("(<=)") {
                        parameter("a")
                        parameter("a")
                    }
                }
                type(NoAttributes, "Str") {
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
                                setOf(CommonAttribute.Internal),
                                listOf(
                                    Alias("Num"),
                                    Alias("Ord")
                                )
                            ),
                            "(Num & Ord)"
                        )
                    }
                    should("TypeOrdNum intersection type") {
                        invoke(get("TypeOrdNum").valueOrNull!!).shouldBeType(
                            IntersectionType(
                                NoAttributes,
                                listOf(
                                    Alias("Num"),
                                    Alias("Ord")
                                )
                            ),
                            "(Num & Ord)"
                        )
                    }
                }
                should("Should have 6 types") {
                    size.shouldBe(6)
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
                type(setOf(CommonAttribute.Public), "int")
                type(setOf(CommonAttribute.Public), "Int")
                type(NoAttributes, "PInt") {
                    parametricType("Int") {}
                }
                parametricType(setOf(CommonAttribute.Public), "Test") {}
                type(NoAttributes, "Float") {
                    alias("Double")
                }
                parametricType(setOf(CommonAttribute.Public), "Float") {
                    type("Double")
                }
                type(setOf(CommonAttribute.Public), "Int")
            }.apply {
                should("Should have Int type") {
                    get("Int").shouldBeType(Concrete(setOf(CommonAttribute.Public), "Int"), "Int")
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
                type(setOf(CommonAttribute.Public), "String")
                type(setOf(CommonAttribute.Public), "Int")
                parametricType(setOf(CommonAttribute.Public), "Num") {
                    parameter("x")
                }
                parametricType(setOf(CommonAttribute.Public), "Map") {
                    parameter("k", "key")
                    parameter("v", "value")
                }
                type(setOf(CommonAttribute.Public), "User") {
                    tupleType {
                        type("String", "name")
                        type("String", "password")
                    }
                }
                type(setOf(CommonAttribute.Public), "Point") {
                    tupleType {
                        parameter("n", "x")
                        parameter("n", "y")
                    }
                }
                type(setOf(CommonAttribute.Public), "Point2D") {
                    tupleType {
                        parametricType("Num", "x") {
                            parameter("n")
                        }
                        parametricType("Num", "y") {
                            parameter("n")
                        }
                    }
                }
                type(setOf(CommonAttribute.Public), "Sum") {
                    functionType {
                        type("Int", "a")
                        parameter("b")
                        type("Int", "result")
                    }
                }
                type(setOf(CommonAttribute.Public), "Option") {
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
                        get("String").shouldBeType(Concrete(setOf(CommonAttribute.Public), "String"), "String")
                    }
                    should("Int type") {
                        get("Int").shouldBeType(Concrete(setOf(CommonAttribute.Public), "Int"), "Int")
                    }
                    should("(Num x) type") {
                        get("Num").shouldBeType(
                            ParametricType(
                                setOf(CommonAttribute.Public),
                                Alias("Num"),
                                listOf(Parameter("x"))
                            ), "(Num x)"
                        )
                    }
                    should("(Map key: k value: v) type") {
                        get("Map").shouldBeType(
                            ParametricType(
                                setOf(CommonAttribute.Public),
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
                                setOf(CommonAttribute.Public),
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
                                setOf(CommonAttribute.Public),
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
                                setOf(CommonAttribute.Public),
                                listOf(
                                    ParametricType(
                                        NoAttributes,
                                        Alias("Num"),
                                        listOf(Parameter("n"))
                                    ).labeled("x"),
                                    ParametricType(
                                        NoAttributes,
                                        Alias("Num"),
                                        listOf(Parameter("n"))
                                    ).labeled("y")
                                )
                            ),
                            "(x: (Num n), y: (Num n))"
                        )
                    }
                    should("(a: Int -> b -> result: Int) Type") {
                        get("Sum").shouldBeType(
                            FunctionType(
                                setOf(CommonAttribute.Public),
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
                                setOf(CommonAttribute.Public),
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
                type(NoAttributes, "Abs") {
                    functionType {
                        type("Int")
                        type("Int")
                    }
                }
                type(setOf(CommonAttribute.Public), "Int")
            }.apply {
                context("Should contain the following types:") {
                    should("Int type") {
                        get("Int").shouldBeType(Concrete(setOf(CommonAttribute.Public), "Int"), "Int")
                    }
                    should("Int -> Int type") {
                        get("Abs").shouldBeType(
                            FunctionType(
                                NoAttributes,
                                listOf(
                                    Alias("Int"),
                                    Alias("Int")
                                )
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
                type(setOf(CommonAttribute.Public), "Int")
            }) {
                parametricType(setOf(CommonAttribute.Public), "List") {
                    type("Int")
                }
            }.apply {
                should("No have errors") {
                    errors.shouldBeEmpty()
                }
                should("Can find Int") {
                    get("Int").shouldBeType(Concrete(setOf(CommonAttribute.Public), "Int"), "Int")
                }
                should("Can find List Int") {
                    get("List").shouldBeType(
                        ParametricType(
                            setOf(CommonAttribute.Public),
                            Alias("List"),
                            listOf(Alias("Int"))
                        ), "(List Int)"
                    )
                }
            }
        }
        context("Hierarchical type system with Errors") {
            typeSystem(typeSystem {
                type(setOf(CommonAttribute.Public), "Int")
                parametricType(setOf(CommonAttribute.Public), "String") {
                    type("Array")
                }
            }) {
                type(setOf(CommonAttribute.Public), "Int")
                parametricType(setOf(CommonAttribute.Public), "Set") {
                    type("String")
                }
                parametricType(setOf(CommonAttribute.Public), "List") {
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
                    get("Int").shouldBeType(Concrete(setOf(CommonAttribute.Public), "Int"), "Int")
                }
                should("Can find List Int") {
                    get("List").shouldBeType(
                        ParametricType(
                            setOf(CommonAttribute.Public),
                            Alias("List"),
                            listOf(Alias("Int"))
                        ), "(List Int)"
                    )
                }
            }
        }
        context("Module type system") {
            moduleTypeSystem {
                register("num", typeSystem {
                    type(setOf(CommonAttribute.Public), "Int")
                    type(setOf(CommonAttribute.Public), "Float")
                })
                register("str", typeSystem {
                    type(setOf(CommonAttribute.Public), "String")
                })
            }.apply {
                errors.shouldBeEmpty()
                size.shouldBe(0)
                get("num.Int").shouldBeType(Concrete(setOf(CommonAttribute.Public), "Int"), "Int")
            }
        }
        context("Module type system with parent") {
            moduleTypeSystem(typeSystem {
                type(setOf(CommonAttribute.Public), "List")
            }) {
                register("num", typeSystem {
                    type(setOf(CommonAttribute.Public), "Int")
                    type(setOf(CommonAttribute.Public), "Float")
                })
                register("str", typeSystem {
                    type(setOf(CommonAttribute.Public), "String")
                })
            }.apply {
                errors.shouldBeEmpty()
                size.shouldBe(0)
                get("List").shouldBeType(Concrete(setOf(CommonAttribute.Public), "List"), "List")
                get("num.Int").shouldBeType(Concrete(setOf(CommonAttribute.Public), "Int"), "Int")
            }
        }
        context("Module type system with parent and errors") {
            moduleTypeSystem(typeSystem {
                type(setOf(CommonAttribute.Public), "List")
                type(setOf(CommonAttribute.Public), "List")
            }) {
                register("num", typeSystem {
                    type(setOf(CommonAttribute.Public), "Int")
                    type(setOf(CommonAttribute.Public), "Int")
                    type(setOf(CommonAttribute.Public), "Float")
                })
                register("str", typeSystem {
                    type(setOf(CommonAttribute.Public), "String")
                })
            }.apply {
                errors.shouldBe(
                    listOf(
                        TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to "List"),
                        TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to "Int"),
                    )
                )
                size.shouldBe(0)
                get("List").shouldBeType(Concrete(setOf(CommonAttribute.Public), "List"), "List")
                get("num.Int").shouldBeType(Concrete(setOf(CommonAttribute.Public), "Int"), "Int")
                get("BigInt").shouldBeLeft()
                get("num.BigDec").shouldBeLeft()
                get("txt.String").shouldBeLeft()
            }
        }
        context("Check list all types on the typeSystem") {
            typeSystem {
                type(setOf(CommonAttribute.Public), "Int")
                type(setOf(CommonAttribute.Public), "String")
            }.apply {
                value.asSequence()
                    .toList()
                    .shouldBe(
                        listOf(
                            "Int" to Concrete(setOf(CommonAttribute.Public), "Int"),
                            "String" to Concrete(setOf(CommonAttribute.Public), "String")
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
