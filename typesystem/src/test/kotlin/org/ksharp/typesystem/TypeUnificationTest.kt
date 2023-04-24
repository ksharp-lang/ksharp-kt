package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.annotations.Annotation
import org.ksharp.typesystem.types.*
import org.ksharp.typesystem.unification.unify

class TypeUnificationTest : StringSpec({
    val typeSystem = typeSystem {
        type("Int")
        type("Long")
        alias("Integer") {
            type("Int")
        }
        parametricType("Map") {
            parameter("a")
            parameter("b")
        }
        parametricType("LongMap") {
            type("Long")
            parameter("b")
        }
    }.apply {
        errors.shouldBeEmpty()
    }.value
    "Unify two parameters" {
        val type1 = newParameter()
        val type2 = newParameter()
        typeSystem.unify(Location.NoProvided, type1, type2).shouldBeRight(type2)
    }
    "Compatible concrete types" {
        val type1 = typeSystem["Int"].valueOrNull!!
        val type2 = typeSystem["Int"].valueOrNull!!
        typeSystem.unify(Location.NoProvided, type1, type2).shouldBeRight(type1)
    }
    "Compatible concrete and parameter types" {
        val type1 = typeSystem["Int"].valueOrNull!!
        val type2 = newParameter()
        typeSystem.unify(Location.NoProvided, type1, type2).shouldBeRight(type1)
        typeSystem.unify(Location.NoProvided, type2, type1).shouldBeRight(type1)
    }
    "Compatible concrete and alias types" {
        val type1 = typeSystem["Int"].valueOrNull!!
        val type2 = typeSystem["Integer"].valueOrNull!!
        typeSystem.unify(Location.NoProvided, type1, type2).shouldBeRight(type1)
    }
    "Compatible concrete and labeled alias types" {
        val type1 = typeSystem["Int"].valueOrNull!!
        val type2 = Labeled("x", typeSystem["Integer"].valueOrNull!!)
        typeSystem.unify(Location.NoProvided, type1, type2).shouldBeRight(type1)
        typeSystem.unify(Location.NoProvided, type2, type1).shouldBeRight(type1)
    }
    "Compatible labeled and alias types" {
        val type1 = Labeled("x", typeSystem["Int"].valueOrNull!!)
        val type2 = typeSystem["Integer"].valueOrNull!!
        typeSystem.unify(Location.NoProvided, type1, type2).shouldBeRight(typeSystem["Int"].valueOrNull!!)
        typeSystem.unify(Location.NoProvided, type2, type1).shouldBeRight(typeSystem["Int"].valueOrNull!!)
    }
    "Compatible concrete and annotated alias types" {
        val type1 = typeSystem["Int"].valueOrNull!!
        val type2 = Annotated(listOf(Annotation("a", mapOf("key" to "value"))), typeSystem["Integer"].valueOrNull!!)
        typeSystem.unify(Location.NoProvided, type1, type2).shouldBeRight(type1)
        typeSystem.unify(Location.NoProvided, type2, type1).shouldBeRight(type1)
    }
    "Not compatible concrete types" {
        val type1 = typeSystem["Int"].valueOrNull!!
        val type2 = typeSystem["Long"].valueOrNull!!
        typeSystem.unify(Location.NoProvided, type1, type2).shouldBeLeft(
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
            Alias("Map"), listOf(
                typeSystem["Integer"].valueOrNull!!,
                typeSystem["Int"].valueOrNull!!
            )
        )
        typeSystem.unify(Location.NoProvided, type1, type2)
            .shouldBeRight(
                ParametricType(
                    Alias("Map"), listOf(
                        typeSystem["Int"].valueOrNull!!,
                        typeSystem["Int"].valueOrNull!!
                    )
                )
            )
    }
    "Compatible parametric type 2" {
        val type1 = typeSystem["Map"].valueOrNull!!
        val type2 = newParameter()
        typeSystem.unify(Location.NoProvided, type1, type2)
            .shouldBeRight(
                ParametricType(
                    Alias("Map"), listOf(
                        Parameter("a"),
                        Parameter("b"),
                    )
                )
            )
    }
    "Incompatible parametric types" {
        val type1 = typeSystem["Map"].valueOrNull!!
        val type2 = ParametricType(
            Alias("Map"), listOf(
                typeSystem["Int"].valueOrNull!!
            )
        )
        typeSystem.unify(Location.NoProvided, type1, type2)
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
            Alias("List"), listOf(
                typeSystem["Int"].valueOrNull!!
            )
        )
        typeSystem.unify(Location.NoProvided, type1, type2)
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
            listOf(
                typeSystem["Int"].valueOrNull!!,
                typeSystem["Long"].valueOrNull!!
            )
        )
        val type2 = TupleType(
            listOf(
                typeSystem["Integer"].valueOrNull!!,
                typeSystem["Long"].valueOrNull!!
            )
        )
        typeSystem.unify(Location.NoProvided, type1, type2)
            .shouldBeRight(
                TupleType(
                    listOf(
                        typeSystem["Int"].valueOrNull!!,
                        typeSystem["Long"].valueOrNull!!
                    )
                )
            )
    }
    "Compatible tuples type 2" {
        val type1 = TupleType(
            listOf(
                typeSystem["Int"].valueOrNull!!,
                typeSystem["Long"].valueOrNull!!
            )
        )
        val type2 = newParameter()
        typeSystem.unify(Location.NoProvided, type1, type2)
            .shouldBeRight(
                TupleType(
                    listOf(
                        typeSystem["Int"].valueOrNull!!,
                        typeSystem["Long"].valueOrNull!!
                    )
                )
            )
    }
    "Incompatible tuples type byt size" {
        val type1 = TupleType(
            listOf(
                typeSystem["Int"].valueOrNull!!,
                typeSystem["Long"].valueOrNull!!
            )
        )
        val type2 = TupleType(
            listOf(
                typeSystem["Long"].valueOrNull!!
            )
        )
        typeSystem.unify(Location.NoProvided, type1, type2)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to type1.representation,
                    "type2" to type2.representation
                )
            )
    }
})