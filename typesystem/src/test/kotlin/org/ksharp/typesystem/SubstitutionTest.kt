package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.substitution.SubstitutionContext
import org.ksharp.typesystem.substitution.extract
import org.ksharp.typesystem.substitution.substitute
import org.ksharp.typesystem.types.*

class SubstitutionContextTest : StringSpec({
    "Substitution adding mappings" {
        val ts = typeSystem {
            type("Int")
            type("String")
        }.value
        val context = SubstitutionContext(ts)
        val intType = ts["Int"].valueOrNull!!
        context.addMapping(Location.NoProvided, "a", intType)
            .shouldBeRight(true)
        context.getMapping(Location.NoProvided, "a", newParameter())
            .shouldBeRight(intType)
    }
    "Substitution mapping can't unify" {
        val ts = typeSystem {
            type("Int")
            type("String")
        }.value
        val context = SubstitutionContext(ts)
        val intType = ts["Int"].valueOrNull!!
        val strType = ts["String"].valueOrNull!!
        context.addMapping(Location.NoProvided, "a", intType)
            .shouldBeRight(true)
        context.addMapping(Location.NoProvided, "a", strType)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to strType.representation,
                    "type2" to intType.representation
                )
            )
        context.getMapping(Location.NoProvided, "a", newParameter())
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to strType.representation,
                    "type2" to intType.representation
                )
            )
    }
    "Substitution not registered" {
        val ts = typeSystem {
            type("Int")
            type("String")
        }.value
        val param = newParameter()
        val context = SubstitutionContext(ts)
        context.getMapping(Location.NoProvided, "a", param)
            .shouldBeLeft(
                TypeSystemErrorCode.SubstitutionNotFound.new(
                    Location.NoProvided,
                    "param" to "a",
                    "type" to param.representation
                )
            )
    }
})

class SubstitutionTest : StringSpec({
    val ts = typeSystem {
        type("Int")
        type("String")
    }.value
    val intType = ts["Int"].valueOrNull!!
    "Concrete type substitution" {
        val context = SubstitutionContext(ts)
        context.extract(Location.NoProvided, intType, intType)
            .shouldBeRight(false)
        context.substitute(Location.NoProvided, intType, intType)
            .shouldBeRight(intType)
        context.errors.build().shouldBeEmpty()
        context.mappings.build().shouldBeEmpty()
    }
    "Parameter type substitution" {
        val context = SubstitutionContext(ts)
        val parameter = newParameter()
        context.extract(Location.NoProvided, parameter, intType)
            .shouldBeRight(true)
        context.substitute(Location.NoProvided, Concrete("String"), intType)
            .shouldBeRight(Concrete("String"))
        context.substitute(Location.NoProvided, parameter, intType)
            .shouldBeRight(intType)
        context.substitute(Location.NoProvided, Parameter("b"), intType)
            .shouldBeLeft(
                TypeSystemErrorCode.SubstitutionNotFound.new(
                    Location.NoProvided,
                    "param" to "b",
                    "type" to "Int"
                )
            )
        context.errors.build().shouldBeEmpty()
        context.mappings.build().shouldBe(
            mapOf(
                parameter.name to intType
            )
        )
    }
    "No compatible parameters" {
        val context = SubstitutionContext(ts)
        val parameter = newParameter()
        context.extract(Location.NoProvided, parameter, intType)
            .shouldBeRight(true)
        context.extract(Location.NoProvided, parameter, Concrete("String"))
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to "String",
                    "type2" to "Int"
                )
            )
        context.substitute(Location.NoProvided, parameter, intType)
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to "String",
                    "type2" to "Int"
                )
            )
        context.errors.build().shouldBe(
            mapOf(
                parameter.name to TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to "String",
                    "type2" to "Int"
                )
            )
        )
        context.mappings.build().shouldBe(
            mapOf(
                parameter.name to intType
            )
        )
    }
    "Alias substitution" {
        val context = SubstitutionContext(ts)
        context.extract(Location.NoProvided, Alias("Int"), intType)
            .shouldBeRight(false)
        context.substitute(Location.NoProvided, Alias("Int"), intType)
            .shouldBeRight(intType)
        context.errors.build().shouldBeEmpty()
        context.mappings.build().shouldBeEmpty()
    }
    "Labeled substitution" {
        val context = SubstitutionContext(ts)
        val parameter = newParameter()
        context.extract(Location.NoProvided, Labeled("lbl", parameter), intType)
            .shouldBeRight(true)
        context.substitute(Location.NoProvided, Labeled("lbl", parameter), intType)
            .shouldBeRight(intType)
        context.errors.build().shouldBeEmpty()
        context.mappings.build().shouldBe(
            mapOf(
                parameter.name to intType
            )
        )
    }
    "Annotated substitution" {
        val context = SubstitutionContext(ts)
        val parameter = newParameter()
        val annotation = Annotated(
            listOf(),
            parameter
        )
        context.extract(Location.NoProvided, annotation, intType)
            .shouldBeRight(true)
        context.substitute(Location.NoProvided, annotation, intType)
            .shouldBeRight(intType)
        context.errors.build().shouldBeEmpty()
        context.mappings.build().shouldBe(
            mapOf(
                parameter.name to intType
            )
        )
    }
    "Tuple substitution" {
        val tuple1 = TupleType(
            listOf(
                Concrete("Int"), newParameter()
            )
        )
        val tuple2 = TupleType(
            listOf(
                Concrete("Int"), Concrete("String")
            )
        )
        val context = SubstitutionContext(ts)
        context.extract(Location.NoProvided, tuple1, tuple2).shouldBeRight(true)
        context.substitute(Location.NoProvided, tuple1, Concrete("Int"))
            .shouldBeRight(tuple2)
    }
    "Tuple substitution error" {
        val tuple1 = TupleType(
            listOf(
                Concrete("Int"), newParameter()
            )
        )
        val tuple2 = TupleType(
            listOf(
                Concrete("Int"), Concrete("String")
            )
        )
        val tuple3 = TupleType(
            listOf(
                Concrete("Int"), Concrete("Int")
            )
        )
        val context = SubstitutionContext(ts)
        context.extract(Location.NoProvided, tuple1, tuple2).shouldBeRight(true)
        context.extract(Location.NoProvided, tuple1, tuple3).shouldBeLeft(
            TypeSystemErrorCode.IncompatibleTypes.new(Location.NoProvided, "type1" to "Int", "type2" to "String")
        )
        context.substitute(Location.NoProvided, tuple1, Concrete("Int"))
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(Location.NoProvided, "type1" to "Int", "type2" to "String")
            )
    }
    "Compound type substitution incompatible error" {
        val tuple1 = TupleType(
            listOf(
                Concrete("Int"), newParameter()
            )
        )
        val context = SubstitutionContext(ts)
        context.extract(Location.NoProvided, tuple1, Concrete("Int"))
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(
                    Location.NoProvided,
                    "type1" to tuple1.representation,
                    "type2" to "Int"
                )
            )
    }
    "Function substitution" {
        val function1 = FunctionType(
            listOf(
                Concrete("Int"), newParameter()
            )
        )
        val function2 = FunctionType(
            listOf(
                Concrete("Int"), Concrete("String")
            )
        )
        val context = SubstitutionContext(ts)
        context.extract(Location.NoProvided, function1, function2).shouldBeRight(true)
        context.substitute(Location.NoProvided, function1, Concrete("Int"))
            .shouldBeRight(function2)
    }
    "Intersection substitution" {
        val type1 = IntersectionType(
            listOf(
                Concrete("Int"), newParameter()
            )
        )
        val type2 = IntersectionType(
            listOf(
                Concrete("Int"), Concrete("String")
            )
        )
        val context = SubstitutionContext(ts)
        context.extract(Location.NoProvided, type1, type2).shouldBeRight(true)
        context.substitute(Location.NoProvided, type1, Concrete("Int"))
            .shouldBeRight(type2)
    }
    "Union substitution" {
        val type1 = UnionType(
            mapOf(
                "Some" to UnionType.ClassType("Some", listOf(newParameter())),
                "None" to UnionType.ClassType("None", emptyList()),
            )
        )
        val type2 = UnionType(
            mapOf(
                "Some" to UnionType.ClassType("Some", listOf(Concrete("Int"))),
                "None" to UnionType.ClassType("None", emptyList()),
            )
        )
        val context = SubstitutionContext(ts)
        context.extract(Location.NoProvided, type1, type2).shouldBeRight(true)
        context.substitute(Location.NoProvided, type1, Concrete("Int"))
            .shouldBeRight(type2)
    }
    "Union substitution incompatible error" {
        val clsType = UnionType.ClassType("Some", listOf(newParameter()))
        val type1 = UnionType(
            mapOf(
                "Some" to clsType,
                "None" to UnionType.ClassType("None", emptyList()),
            )
        )
        val type2 = UnionType(
            mapOf(
                "Some" to UnionType.ClassType("AnotherSome", listOf(Concrete("Int"))),
                "None" to UnionType.ClassType("None", emptyList()),
            )
        )
        val context = SubstitutionContext(ts)
        context.extract(Location.NoProvided, type1, type2).shouldBeLeft(
            TypeSystemErrorCode.IncompatibleTypes.new(
                Location.NoProvided,
                "type1" to clsType.representation,
                "type2" to UnionType.ClassType("AnotherSome", listOf(Concrete("Int"))).representation
            )
        )
        context.substitute(Location.NoProvided, type1, Concrete("Int"))
            .shouldBeLeft(
                TypeSystemErrorCode.SubstitutionNotFound.new(
                    Location.NoProvided,
                    "param" to "@0",
                    "type" to "Int"
                )
            )
    }
})