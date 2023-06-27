package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.sequences.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.*

class TypeTest : StringSpec({
    "Given a type, should get an empty term sequence" {
        Concrete(NoAttributes, "Int").terms.shouldBeEmpty()
    }
    "Given a parameter, should get an empty term sequence" {
        Parameter("a").terms.shouldBeEmpty()
    }
    "Given a labeled type, should get the labeled type as term in the sequence" {
        Concrete(NoAttributes, "Int").labeled("x").terms
            .toList()
            .shouldBe(listOf(Concrete(NoAttributes, "Int")))
    }
    "Given a tuple type, should get the types elements as terms in the sequence" {
        TupleType(
            NoAttributes,
            listOf(Concrete(NoAttributes, "Int"), Concrete(NoAttributes, "Int"))
        ).terms.toList().shouldBe(
            listOf(
                Concrete(NoAttributes, "Int"), Concrete(NoAttributes, "Int")
            )
        )
    }
    "Given a union type, should get the classes elements as terms in the sequence" {
        UnionType(
            NoAttributes,
            mapOf(
                "None" to UnionType.ClassType("None", listOf()),
                "Maybe" to UnionType.ClassType(
                    "Maybe",
                    listOf(Parameter("a"))
                )
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
            NoAttributes,
            Alias("List"),
            listOf(Parameter("a"))
        ).terms.toList().shouldBe(listOf(Alias("List"), Parameter("a")))
    }
    "Given a function type, should get the arguments and return terms in the sequence" {
        FunctionType(
            NoAttributes,
            listOf(
                Concrete(NoAttributes, "Int"),
                Parameter("a"),
                Parameter("b")
            )
        ).terms.toList().shouldBe(
            listOf(
                Concrete(NoAttributes, "Int"),
                Parameter("a"),
                Parameter("b")
            )
        )
    }
    "Check extension function label on types" {
        Concrete(NoAttributes, "Int").label.shouldBeNull()
        Concrete(NoAttributes, "Int").labeled("x").label.shouldBe("x")
    }
    "Check type constructor" {
        TypeConstructor(NoAttributes, "True", "Bool").apply {
            terms.shouldBeEmpty()
            representation.shouldBe("True")
            compound.shouldBeFalse()
        }
    }
})
