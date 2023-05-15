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
        Concrete("Int").terms.shouldBeEmpty()
    }
    "Given a parameter, should get an empty term sequence" {
        Parameter("a").terms.shouldBeEmpty()
    }
    "Given a labeled type, should get the labeled type as term in the sequence" {
        Concrete("Int").labeled("x").terms
            .toList()
            .shouldBe(listOf(Concrete("Int")))
    }
    "Given an annotated type, should get the annotated type as term in the sequence" {
        Concrete("Int").annotated(
            listOf(annotation("pure"))
        ).terms
            .toList()
            .shouldBe(listOf(Concrete("Int")))
    }
    "Given a tuple type, should get the types elements as terms in the sequence" {
        TupleType(
            listOf(Concrete("Int"), Concrete("Int"))
        ).terms.toList().shouldBe(
            listOf(
                Concrete("Int"), Concrete("Int")
            )
        )
    }
    "Given a union type, should get the classes elements as terms in the sequence" {
        UnionType(
            mapOf(
                "None" to UnionType.ClassType("None", listOf()),
                "Maybe" to UnionType.ClassType("Maybe", listOf(Parameter("a")))
            )
        ).terms.toList().shouldBe(
            listOf(
                UnionType.ClassType("None", listOf()),
                UnionType.ClassType("Maybe", listOf(Parameter("a")))
            )
        )
    }
    "Given a Union Class, should get the elements as terms in the sequence" {
        UnionType.ClassType("Maybe", listOf(Parameter("a")))
            .terms.toList().shouldBe(listOf(Parameter("a")))
    }
    "Given a Parametric type, should get the parameters as terms in the sequence" {
        ParametricType(
            Alias("List"),
            listOf(Parameter("a"))
        ).terms.toList().shouldBe(listOf(Alias("List"), Parameter("a")))
    }
    "Given a function type, should get the arguments and return terms in the sequence" {
        FunctionType(
            listOf(
                Concrete("Int"),
                Parameter("a"),
                Parameter("b")
            )
        ).terms.toList().shouldBe(listOf(Concrete("Int"), Parameter("a"), Parameter("b")))
    }
    "Check extension function label on types" {
        Concrete("Int").label.shouldBeNull()
        Concrete("Int").labeled("x").label.shouldBe("x")
    }
    "Check extension function annotations on types" {
        Concrete("Int").annotations.shouldBeEmpty()
        Concrete("Int").annotated(listOf(annotation("pure")))
            .annotations.shouldBe(listOf(annotation("pure")))
    }
    "Check type constructor" {
        TypeConstructor("True", "Bool").apply {
            terms.shouldBeEmpty()
            representation.shouldBe("True")
            compound.shouldBeFalse()
        }
    }
})