package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.sequences.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.typesystem.annotations.annotation
import org.ksharp.typesystem.types.*

class TypeTest : StringSpec({
    "Given a type, should get an empty term sequence" {
        Concrete(TypeVisibility.Public, "Int").terms.shouldBeEmpty()
    }
    "Given a parameter, should get an empty term sequence" {
        Parameter(TypeVisibility.Public, "a").terms.shouldBeEmpty()
    }
    "Given a labeled type, should get the labeled type as term in the sequence" {
        Concrete(TypeVisibility.Public, "Int").labeled("x").terms
            .toList()
            .shouldBe(listOf(Concrete(TypeVisibility.Public, "Int")))
    }
    "Given an annotated type, should get the annotated type as term in the sequence" {
        Concrete(TypeVisibility.Public, "Int").annotated(
            listOf(annotation("pure"))
        ).terms
            .toList()
            .shouldBe(listOf(Concrete(TypeVisibility.Public, "Int")))
    }
    "Given a tuple type, should get the types elements as terms in the sequence" {
        TupleType(
            TypeVisibility.Public,
            listOf(Concrete(TypeVisibility.Public, "Int"), Concrete(TypeVisibility.Public, "Int"))
        ).terms.toList().shouldBe(
            listOf(
                Concrete(TypeVisibility.Public, "Int"), Concrete(TypeVisibility.Public, "Int")
            )
        )
    }
    "Given a union type, should get the classes elements as terms in the sequence" {
        UnionType(
            TypeVisibility.Public,
            mapOf(
                "None" to UnionType.ClassType(TypeVisibility.Public, "None", listOf()),
                "Maybe" to UnionType.ClassType(
                    TypeVisibility.Public,
                    "Maybe",
                    listOf(Parameter(TypeVisibility.Public, "a"))
                )
            )
        ).terms.toList().shouldBe(
            listOf(
                UnionType.ClassType(TypeVisibility.Public, "None", listOf()),
                UnionType.ClassType(TypeVisibility.Public, "Maybe", listOf(Parameter(TypeVisibility.Public, "a")))
            )
        )
    }
    "Given a Union Class, should get the elements as terms in the sequence" {
        UnionType.ClassType(TypeVisibility.Public, "Maybe", listOf(Parameter(TypeVisibility.Public, "a")))
            .terms.toList().shouldBe(listOf(Parameter(TypeVisibility.Public, "a")))
    }
    "Given a Parametric type, should get the parameters as terms in the sequence" {
        ParametricType(
            TypeVisibility.Public,
            Alias(TypeVisibility.Public, "List"),
            listOf(Parameter(TypeVisibility.Public, "a"))
        ).terms.toList().shouldBe(listOf(Alias(TypeVisibility.Public, "List"), Parameter(TypeVisibility.Public, "a")))
    }
    "Given a function type, should get the arguments and return terms in the sequence" {
        FunctionType(
            TypeVisibility.Public,
            listOf(
                Concrete(TypeVisibility.Public, "Int"),
                Parameter(TypeVisibility.Public, "a"),
                Parameter(TypeVisibility.Public, "b")
            )
        ).terms.toList().shouldBe(
            listOf(
                Concrete(TypeVisibility.Public, "Int"),
                Parameter(TypeVisibility.Public, "a"),
                Parameter(TypeVisibility.Public, "b")
            )
        )
    }
    "Check extension function label on types" {
        Concrete(TypeVisibility.Public, "Int").label.shouldBeNull()
        Concrete(TypeVisibility.Public, "Int").labeled("x").label.shouldBe("x")
    }
    "Check extension function annotations on types" {
        Concrete(TypeVisibility.Public, "Int").annotations.shouldBeEmpty()
        Concrete(TypeVisibility.Public, "Int").annotated(listOf(annotation("pure")))
            .annotations.shouldBe(listOf(annotation("pure")))
    }
    "Check type constructor" {
        TypeConstructor(TypeVisibility.Public, "True", "Bool").apply {
            terms.shouldBeEmpty()
            representation.shouldBe("True")
            compound.shouldBeFalse()
        }
    }
})