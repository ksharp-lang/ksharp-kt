package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.*
import org.ksharp.typesystem.unification.UnificationChecker
import org.ksharp.typesystem.unification.unify

class TypeUnificationTest : StringSpec({
    val typeSystem = typeSystem {
        type(NoAttributes, "Int")
        type(NoAttributes, "Long")
        type(NoAttributes, "Integer") {
            alias("Int")
        }
        parametricType(NoAttributes, "Map") {
            parameter("a")
            parameter("b")
        }
        type(NoAttributes, "LongMap") {
            parametricType("Map") {
                type("Long")
                parameter("b")
            }
        }
    }.apply {
        errors.shouldBeEmpty()
    }.value
    val checker = UnificationChecker { _, _ -> false }
    "Unify two parameters" {
        val type1 = typeSystem.newParameter()
        val type2 = typeSystem.newParameter()
        type1.unify(Location.NoProvided, type2, checker).shouldBeRight(type2)
    }
    "Compatible concrete types" {
        val type1 = typeSystem["Int"].valueOrNull!!
        val type2 = typeSystem["Int"].valueOrNull!!
        type1.unify(Location.NoProvided, type2, checker).shouldBeRight(type1)
    }
    "Compatible concrete and parameter types" {
        val type1 = typeSystem["Int"].valueOrNull!!
        val type2 = typeSystem.newParameter()
        type1.unify(Location.NoProvided, type2, checker).shouldBeRight(type1)
        type1.unify(Location.NoProvided, type1, checker).shouldBeRight(type1)
    }
    "Compatible concrete and alias types" {
        val type1 = typeSystem["Int"].valueOrNull!!
        val type2 = typeSystem["Integer"].valueOrNull!!
        type1.unify(Location.NoProvided, type2, checker).shouldBeRight(type1)
    }
    "Compatible concrete and labeled alias types" {
        val type1 = typeSystem["Int"].valueOrNull!!
        val type2 = Labeled("x", typeSystem["Integer"].valueOrNull!!)
        type1.unify(Location.NoProvided, type2, checker).shouldBeRight(type1)
        type2.unify(Location.NoProvided, type1, checker).shouldBeRight(type1)
    }
    "Compatible labeled and alias types" {
        val type1 = Labeled("x", typeSystem["Int"].valueOrNull!!)
        val type2 = typeSystem["Integer"].valueOrNull!!
        type1.unify(Location.NoProvided, type2, checker).shouldBeRight(typeSystem["Int"].valueOrNull!!)
        type2.unify(Location.NoProvided, type1, checker).shouldBeRight(typeSystem["Int"].valueOrNull!!)
    }
    "Not compatible concrete types" {
        val type1 = typeSystem["Int"].valueOrNull!!
        val type2 = typeSystem["Long"].valueOrNull!!
        type1.unify(Location.NoProvided, type2, checker).shouldBeLeft(
            TypeSystemErrorCode.IncompatibleTypes.new(
                Location.NoProvided,
                "type1" to type1.representation,
                "type2" to type2.representation
            )
        )
    }
    "Compatible parametric types" {
        val type1 = typeSystem["Map"].valueOrNull!!
        val type2 = ParametricType(
            typeSystem.handle,
            NoAttributes,
            Alias(typeSystem.handle, "Map"), listOf(
                typeSystem["Integer"].valueOrNull!!,
                typeSystem["Int"].valueOrNull!!
            )
        )
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeRight(
                ParametricType(
                    typeSystem.handle,
                    NoAttributes,
                    Alias(typeSystem.handle, "Map"), listOf(
                        typeSystem["Int"].valueOrNull!!,
                        typeSystem["Int"].valueOrNull!!
                    )
                )
            )
    }
    "Compatible parametric type 2" {
        val type1 = typeSystem["Map"].valueOrNull!!
        val type2 = typeSystem.newParameter()
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeRight(
                ParametricType(
                    typeSystem.handle,
                    NoAttributes,
                    Alias(typeSystem.handle, "Map"), listOf(
                        Parameter(typeSystem.handle, "a"),
                        Parameter(typeSystem.handle, "b"),
                    )
                )
            )
    }
    "Incompatible parametric types" {
        val type1 = typeSystem["Map"].valueOrNull!!
        val type2 = ParametricType(
            typeSystem.handle,
            NoAttributes,
            Alias(typeSystem.handle, "Map"), listOf(
                typeSystem["Int"].valueOrNull!!
            )
        )
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to type1.representation,
                    "type2" to type2.representation
                )
            )
    }
    "Compatible parametric types by types" {
        val type1 = typeSystem["LongMap"].valueOrNull!!
        val type2 = ParametricType(
            typeSystem.handle,
            NoAttributes,
            Alias(typeSystem.handle, "Map"), listOf(
                typeSystem["Long"].valueOrNull!!,
                typeSystem["Int"].valueOrNull!!
            )
        )
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeRight(
                type2
            )
    }
    "Incompatible parametric types by types" {
        val type1 = typeSystem["LongMap"].valueOrNull!!
        val type2 = ParametricType(
            typeSystem.handle,
            NoAttributes,
            Alias(typeSystem.handle, "Map"), listOf(
                typeSystem["Int"].valueOrNull!!,
                typeSystem["Long"].valueOrNull!!
            )
        )
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to type1.representation,
                    "type2" to type2.representation
                )
            )
    }
    "Incompatible parametric types 2" {
        val type1 = typeSystem["Map"].valueOrNull!!
        val type2 = Concrete(typeSystem.handle, NoAttributes, "Int")
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to type1.representation,
                    "type2" to type2.representation
                )
            )
    }
    "Incompatible parametric types by variable" {
        val type1 = typeSystem["Map"].valueOrNull!!
        val type2 = ParametricType(
            typeSystem.handle,
            NoAttributes,
            Alias(typeSystem.handle, "List"), listOf(
                typeSystem["Int"].valueOrNull!!,
                typeSystem["Int"].valueOrNull!!
            )
        )
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to type1.representation,
                    "type2" to type2.representation
                )
            )
    }
    "Compatible tuples type" {
        val type1 = TupleType(
            typeSystem.handle,
            NoAttributes,
            listOf(
                typeSystem["Int"].valueOrNull!!,
                typeSystem["Long"].valueOrNull!!
            )
        )
        val type2 = TupleType(
            typeSystem.handle,
            NoAttributes,
            listOf(
                typeSystem["Integer"].valueOrNull!!,
                typeSystem["Long"].valueOrNull!!
            )
        )
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeRight(
                TupleType(
                    typeSystem.handle,
                    NoAttributes,
                    listOf(
                        typeSystem["Int"].valueOrNull!!,
                        typeSystem["Long"].valueOrNull!!
                    )
                )
            )
    }
    "Compatible tuples type 2" {
        val type1 = TupleType(
            typeSystem.handle,
            NoAttributes,
            listOf(
                typeSystem["Int"].valueOrNull!!,
                typeSystem["Long"].valueOrNull!!
            )
        )
        val type2 = typeSystem.newParameter()
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeRight(
                TupleType(
                    typeSystem.handle,
                    NoAttributes,
                    listOf(
                        typeSystem["Int"].valueOrNull!!,
                        typeSystem["Long"].valueOrNull!!
                    )
                )
            )
    }
    "Incompatible tuples type byt size" {
        val type1 = TupleType(
            typeSystem.handle,
            NoAttributes,
            listOf(
                typeSystem["Int"].valueOrNull!!,
                typeSystem["Long"].valueOrNull!!
            )
        )
        val type2 = TupleType(
            typeSystem.handle,
            NoAttributes,
            listOf(
                typeSystem["Long"].valueOrNull!!
            )
        )
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to type1.representation,
                    "type2" to type2.representation
                )
            )
    }
    "Compatible function type" {
        val type1 = FunctionType(
            typeSystem.handle,
            NoAttributes,
            listOf(
                typeSystem["Int"].valueOrNull!!,
                typeSystem["Long"].valueOrNull!!
            )
        )
        val type2 = FunctionType(
            typeSystem.handle,
            NoAttributes,
            listOf(
                typeSystem["Integer"].valueOrNull!!,
                typeSystem["Long"].valueOrNull!!
            )
        )
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeRight(
                FunctionType(
                    typeSystem.handle,
                    NoAttributes,
                    listOf(
                        typeSystem["Int"].valueOrNull!!,
                        typeSystem["Long"].valueOrNull!!
                    )
                )
            )
    }
    "Incompatible function type" {
        val type1 = FunctionType(
            typeSystem.handle,
            NoAttributes,
            listOf(
                typeSystem["Int"].valueOrNull!!,
                typeSystem["Long"].valueOrNull!!
            )
        )
        val type2 = FunctionType(
            typeSystem.handle,
            NoAttributes,
            listOf(
                typeSystem["Integer"].valueOrNull!!,
                typeSystem["Long"].valueOrNull!!,
                typeSystem["Long"].valueOrNull!!
            )
        )
        type1.unify(Location.NoProvided, type2, checker)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to type1.representation,
                    "type2" to type2.representation
                )
            )
    }
    "Compatible type constructor and union type" {
        val union = UnionType(
            typeSystem.handle,
            NoAttributes,
            mapOf(
                "True" to UnionType.ClassType(typeSystem.handle, "True", listOf()),
                "False" to UnionType.ClassType(typeSystem.handle, "True", listOf())
            )
        )
        val typeConstructor = TypeConstructor(typeSystem.handle, NoAttributes, "True", "Bool")
        union.unify(Location.NoProvided, typeConstructor, checker)
            .shouldBeRight(union)
        union.unify(
            Location.NoProvided, UnionType(
                typeSystem.handle,
                NoAttributes,
                mapOf(
                    "True" to UnionType.ClassType(typeSystem.handle, "True", listOf()),
                    "False" to UnionType.ClassType(typeSystem.handle, "True", listOf())
                )
            ),
            checker
        ).shouldBeRight(union)
        union.unify(Location.NoProvided, Concrete(typeSystem.handle, NoAttributes, "Int"), checker)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to union.representation,
                    "type2" to "Int"
                )
            )
        union.unify(Location.NoProvided, TypeConstructor(typeSystem.handle, NoAttributes, "Other", "Bool"), checker)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to union.representation,
                    "type2" to "Other"
                )
            )
    }
    "Unification Parametric with Generalise types" {
        val ts = typeSystem {
            type(NoAttributes, "NativeInt")
            parametricType(NoAttributes, "Num") {
                parameter("a")
            }
            type(NoAttributes, "Int") {
                parametricType("Num") {
                    type("NativeInt")
                }
            }
        }.value
        ParametricType(
            ts.handle,
            NoAttributes, Concrete(ts.handle, NoAttributes, "Num"), listOf(
                Parameter(ts.handle, "a")
            )
        ).unify(
            Location.NoProvided,
            Alias(ts.handle, "Int"),
            checker
        ).shouldBeRight(
            ParametricType(
                ts.handle,
                NoAttributes, Concrete(ts.handle, NoAttributes, "Num"), listOf(
                    Concrete(ts.handle, NoAttributes, "NativeInt")
                )
            )
        )
    }
})
