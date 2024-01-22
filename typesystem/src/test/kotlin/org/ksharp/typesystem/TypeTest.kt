package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.sequences.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.*

class TypeTest : StringSpec({
    val ts = typeSystem { }.value
    "Given a type, should get an empty term sequence" {
        Concrete(ts.handle, NoAttributes, "Int").terms.shouldBeEmpty()
    }
    "Given a parameter, should get an empty term sequence" {
        Parameter(ts.handle, "a").terms.shouldBeEmpty()
    }
    "Given a labeled type, should get the labeled type as term in the sequence" {
        Concrete(ts.handle, NoAttributes, "Int").labeled("x").terms
            .toList()
            .shouldBe(listOf(Concrete(ts.handle, NoAttributes, "Int")))
    }
    "Given a tuple type, should get the types elements as terms in the sequence" {
        TupleType(
            ts.handle,
            NoAttributes,
            listOf(Concrete(ts.handle, NoAttributes, "Int"), Concrete(ts.handle, NoAttributes, "Int"))
        ).terms.toList().shouldBe(
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"), Concrete(ts.handle, NoAttributes, "Int")
            )
        )
    }
    "Given a union type, should get the classes elements as terms in the sequence" {
        UnionType(
            ts.handle,
            NoAttributes,
            mapOf(
                "None" to UnionType.ClassType(ts.handle, "None", listOf()),
                "Maybe" to UnionType.ClassType(
                    ts.handle,
                    "Maybe",
                    listOf(Parameter(ts.handle, "a"))
                )
            )
        ).terms.toList().shouldBe(
            listOf(
                UnionType.ClassType(ts.handle, "None", listOf()),
                UnionType.ClassType(ts.handle, "Maybe", listOf(Parameter(ts.handle, "a")))
            )
        )
    }
    "Given a Union Class, should get the elements as terms in the sequence" {
        UnionType.ClassType(ts.handle, "Maybe", listOf(Parameter(ts.handle, "a")))
            .terms.toList().shouldBe(listOf(Parameter(ts.handle, "a")))
    }
    "Given a Parametric type, should get the parameters as terms in the sequence" {
        ParametricType(
            ts.handle,
            NoAttributes,
            Alias(ts.handle, "List"),
            listOf(Parameter(ts.handle, "a"))
        ).terms.toList().shouldBe(listOf(Alias(ts.handle, "List"), Parameter(ts.handle, "a")))
    }
    "Given a function type, should get the arguments and return terms in the sequence" {
        FullFunctionType(
            ts.handle,
            NoAttributes,
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"),
                Parameter(ts.handle, "a"),
                Parameter(ts.handle, "b")
            ),
            ModuleFunctionScope
        ).terms.toList().shouldBe(
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"),
                Parameter(ts.handle, "a"),
                Parameter(ts.handle, "b")
            )
        )
    }
    "Check extension function label on types" {
        Concrete(ts.handle, NoAttributes, "Int").label.shouldBeNull()
        Concrete(ts.handle, NoAttributes, "Int").labeled("x").label.shouldBe("x")
    }
    "Check type constructor" {
        TypeConstructor(ts.handle, NoAttributes, "True", "Bool").apply {
            terms.shouldBeEmpty()
            representation.shouldBe("True")
            compound.shouldBeFalse()
        }
    }
})
