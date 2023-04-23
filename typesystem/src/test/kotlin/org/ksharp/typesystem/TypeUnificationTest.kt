package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
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
        typeSystem.unify(Location.NoProvided, type1, type2).shouldBeRight(typeSystem(type2).valueOrNull!!)
        typeSystem.unify(Location.NoProvided, type2, type1).shouldBeRight(type1)
    }
    "Compatible labeled and alias types" {
        val type1 = Labeled("x", typeSystem["Int"].valueOrNull!!)
        val type2 = typeSystem["Integer"].valueOrNull!!
        typeSystem.unify(Location.NoProvided, type1, type2).shouldBeRight(typeSystem["Int"].valueOrNull!!)
        typeSystem.unify(Location.NoProvided, type2, type1).shouldBeRight(type1)
    }
    "Compatible concrete and annotated alias types" {
        val type1 = typeSystem["Int"].valueOrNull!!
        val type2 = Annotated(listOf(Annotation("a", mapOf("key" to "value"))), typeSystem["Integer"].valueOrNull!!)
        typeSystem.unify(Location.NoProvided, type1, type2).shouldBeRight(typeSystem(type2).valueOrNull!!)
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
})