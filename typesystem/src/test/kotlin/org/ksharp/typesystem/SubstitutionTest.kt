package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.substitution.SubstitutionContext
import org.ksharp.typesystem.substitution.extract
import org.ksharp.typesystem.substitution.substitute
import org.ksharp.typesystem.types.*
import org.ksharp.typesystem.unification.UnificationChecker

class SubstitutionContextTest : StringSpec({
    val checker = UnificationChecker { _, _ -> false }
    "Substitution adding mappings" {
        val ts = typeSystem {
            type(NoAttributes, "Int")
            type(NoAttributes, "String")
        }.value
        val context = SubstitutionContext(checker)
        val intType = ts["Int"].valueOrNull!!
        context.addMapping(Location.NoProvided, "a", intType)
            .shouldBeRight(true)
        context.getMapping(Location.NoProvided, "a", ts.newParameter())
            .shouldBeRight(intType)
    }
    "Substitution mapping can't unify" {
        val ts = typeSystem {
            type(NoAttributes, "Int")
            type(NoAttributes, "String")
        }.value
        val context = SubstitutionContext(checker)
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
        context.getMapping(Location.NoProvided, "a", ts.newParameter())
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
            type(NoAttributes, "Int")
            type(NoAttributes, "String")
        }.value
        val param = ts.newParameter()
        val context = SubstitutionContext(checker)
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
    val checker = UnificationChecker { _, _ -> false }
    val ts = typeSystem {
        type(NoAttributes, "Int")
        type(NoAttributes, "String")
    }.value
    val intType = ts["Int"].valueOrNull!!
    "Concrete type substitution" {
        val context = SubstitutionContext(checker)
        context.extract(Location.NoProvided, intType, intType)
            .shouldBeRight(false)
        context.substitute(Location.NoProvided, intType, intType)
            .shouldBeRight(intType)
        context.errors.build().shouldBeEmpty()
        context.mappings.build().shouldBeEmpty()
    }
    "Parameter type substitution" {
        val context = SubstitutionContext(checker)
        val parameter = ts.newParameter()
        context.extract(Location.NoProvided, parameter, intType)
            .shouldBeRight(true)
        context.substitute(Location.NoProvided, Concrete(ts.handle, NoAttributes, "String"), intType)
            .shouldBeRight(Concrete(ts.handle, NoAttributes, "String"))
        context.substitute(Location.NoProvided, parameter, intType)
            .shouldBeRight(intType)
        context.substitute(Location.NoProvided, Parameter(ts.handle, "b"), intType)
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
        val context = SubstitutionContext(checker)
        val parameter = ts.newParameter()
        context.extract(Location.NoProvided, parameter, intType)
            .shouldBeRight(true)
        context.extract(Location.NoProvided, parameter, Concrete(ts.handle, NoAttributes, "String"))
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
        val context = SubstitutionContext(checker)
        context.extract(Location.NoProvided, Alias(ts.handle, "Int"), intType)
            .shouldBeRight(false)
        context.substitute(Location.NoProvided, Alias(ts.handle, "Int"), intType)
            .shouldBeRight(intType)
        context.errors.build().shouldBeEmpty()
        context.mappings.build().shouldBeEmpty()
    }
    "Labeled substitution" {
        val context = SubstitutionContext(checker)
        val parameter = ts.newParameter()
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
    "Tuple substitution" {
        val tuple1 = TupleType(
            ts.handle,
            NoAttributes,
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"), ts.newParameter()
            )
        )
        val tuple2 = TupleType(
            ts.handle,
            NoAttributes,
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"), Concrete(ts.handle, NoAttributes, "String")
            )
        )
        val context = SubstitutionContext(checker)
        context.extract(Location.NoProvided, tuple1, tuple2).shouldBeRight(true)
        context.substitute(Location.NoProvided, tuple1, Concrete(ts.handle, NoAttributes, "Int"))
            .shouldBeRight(tuple2)
    }
    "Tuple substitution error" {
        val tuple1 = TupleType(
            ts.handle,
            NoAttributes,
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"), ts.newParameter()
            )
        )
        val tuple2 = TupleType(
            ts.handle,
            NoAttributes,
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"), Concrete(ts.handle, NoAttributes, "String")
            )
        )
        val tuple3 = TupleType(
            ts.handle,
            NoAttributes,
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"), Concrete(ts.handle, NoAttributes, "Int")
            )
        )
        val context = SubstitutionContext(checker)
        context.extract(Location.NoProvided, tuple1, tuple2).shouldBeRight(true)
        context.extract(Location.NoProvided, tuple1, tuple3).shouldBeLeft(
            TypeSystemErrorCode.IncompatibleTypes.new(Location.NoProvided, "type1" to "Int", "type2" to "String")
        )
        context.substitute(Location.NoProvided, tuple1, Concrete(ts.handle, NoAttributes, "Int"))
            .shouldBeLeft(
                TypeSystemErrorCode.IncompatibleTypes.new(Location.NoProvided, "type1" to "Int", "type2" to "String")
            )
    }
    "Compound type substitution incompatible error" {
        val tuple1 = TupleType(
            ts.handle,
            NoAttributes,
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"), ts.newParameter()
            )
        )
        val context = SubstitutionContext(checker)
        context.extract(Location.NoProvided, tuple1, Concrete(ts.handle, NoAttributes, "Int"))
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
            ts.handle,
            NoAttributes,
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"), ts.newParameter()
            )
        )
        val function2 = FunctionType(
            ts.handle,
            NoAttributes,
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"), Concrete(ts.handle, NoAttributes, "String")
            )
        )
        val context = SubstitutionContext(checker)
        context.extract(Location.NoProvided, function1, function2).shouldBeRight(true)
        context.substitute(Location.NoProvided, function1, Concrete(ts.handle, NoAttributes, "Int"))
            .shouldBeRight(function2)
    }
    "Intersection substitution" {
        val type1 = IntersectionType(
            ts.handle,
            NoAttributes,
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"), ts.newParameter()
            )
        )
        val type2 = IntersectionType(
            ts.handle,
            NoAttributes,
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"), Concrete(ts.handle, NoAttributes, "String")
            )
        )
        val context = SubstitutionContext(checker)
        context.extract(Location.NoProvided, type1, type2).shouldBeRight(true)
        context.substitute(Location.NoProvided, type1, Concrete(ts.handle, NoAttributes, "Int"))
            .shouldBeRight(type2)
    }
    "Union substitution" {
        val type1 = UnionType(
            ts.handle,
            NoAttributes,
            mapOf(
                "Some" to UnionType.ClassType(ts.handle, "Some", listOf(ts.newParameter())),
                "None" to UnionType.ClassType(ts.handle, "None", emptyList()),
            )
        )
        val type2 = UnionType(
            ts.handle,
            NoAttributes,
            mapOf(
                "Some" to UnionType.ClassType(
                    ts.handle,
                    "Some",
                    listOf(Concrete(ts.handle, NoAttributes, "Int"))
                ),
                "None" to UnionType.ClassType(ts.handle, "None", emptyList()),
            )
        )
        val context = SubstitutionContext(checker)
        context.extract(Location.NoProvided, type1, type2).shouldBeRight(true)
        context.substitute(Location.NoProvided, type1, Concrete(ts.handle, NoAttributes, "Int"))
            .shouldBeRight(type2)
    }
    "Union substitution incompatible error" {
        val param = ts.newParameter()
        val clsType = UnionType.ClassType(ts.handle, "Some", listOf(param))
        val type1 = UnionType(
            ts.handle,
            NoAttributes,
            mapOf(
                "Some" to clsType,
                "None" to UnionType.ClassType(ts.handle, "None", emptyList()),
            )
        )
        val type2 = UnionType(
            ts.handle,
            NoAttributes,
            mapOf(
                "Some" to UnionType.ClassType(
                    ts.handle,
                    "AnotherSome",
                    listOf(Concrete(ts.handle, NoAttributes, "Int"))
                ),
                "None" to UnionType.ClassType(ts.handle, "None", emptyList()),
            )
        )
        val context = SubstitutionContext(checker)
        context.extract(Location.NoProvided, type1, type2).shouldBeLeft(
            TypeSystemErrorCode.IncompatibleTypes.new(
                Location.NoProvided,
                "type1" to clsType.representation,
                "type2" to UnionType.ClassType(
                    ts.handle,
                    "AnotherSome",
                    listOf(Concrete(ts.handle, NoAttributes, "Int"))
                ).representation
            )
        )
        context.substitute(Location.NoProvided, type1, Concrete(ts.handle, NoAttributes, "Int"))
            .shouldBeLeft(
                TypeSystemErrorCode.SubstitutionNotFound.new(
                    Location.NoProvided,
                    "param" to param.representation,
                    "type" to "Int"
                )
            )
    }
    "Parametric type substitution" {
        val type1 = ParametricType(
            ts.handle,
            NoAttributes,
            Concrete(ts.handle, NoAttributes, "Map"),
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"), ts.newParameter()
            )
        )
        val type2 = ParametricType(
            ts.handle,
            NoAttributes,
            Concrete(ts.handle, NoAttributes, "Map"),
            listOf(
                Concrete(ts.handle, NoAttributes, "Int"), Concrete(ts.handle, NoAttributes, "String")
            )
        )
        val context = SubstitutionContext(checker)
        context.extract(Location.NoProvided, type1, type2).shouldBeRight(true)
        context.substitute(Location.NoProvided, type1, Concrete(ts.handle, NoAttributes, "Int"))
            .shouldBeRight(type2)
    }
    "Parametric type substitution using alias types" {
        val typeSystem = typeSystem {
            parametricType(NoAttributes, "Num") {
                parameter("a")
            }
        }.value
        val context = SubstitutionContext(checker)
        val p1 = ParametricType(
            typeSystem.handle,
            NoAttributes,
            Alias(typeSystem.handle, "Num"),
            listOf(Parameter(typeSystem.handle, "a"))
        )
        val p2 = ParametricType(
            typeSystem.handle,
            NoAttributes,
            Alias(typeSystem.handle, "Num"),
            listOf(Parameter(typeSystem.handle, "a"))
        )
        context.extract(Location.NoProvided, p1, p2).shouldBeRight(true)
        context.substitute(Location.NoProvided, p1, Concrete(typeSystem.handle, NoAttributes, "Int"))
            .shouldBeRight(p1)
    }
    "Parametric type substitution using alias types and concrete types" {
        val typeSystem = typeSystem {
            parametricType(NoAttributes, "Num") {
                parameter("a")
            }
            type(NoAttributes, "Int")
        }.value
        val context = SubstitutionContext(checker)
        val p1 = ParametricType(
            typeSystem.handle,
            NoAttributes,
            Alias(typeSystem.handle, "Num"),
            listOf(Parameter(typeSystem.handle, "a"))
        )
        val p2 = ParametricType(
            typeSystem.handle,
            NoAttributes,
            Alias(typeSystem.handle, "Num"),
            listOf(Concrete(typeSystem.handle, NoAttributes, "Int"))
        )
        context.extract(Location.NoProvided, p1, p2).shouldBeRight(true)
        context.substitute(Location.NoProvided, p1, Concrete(typeSystem.handle, NoAttributes, "Int"))
            .shouldBeRight(p2)
    }
    "Parametric type substitution using alias types and specialized parametric type" {
        val typeSystem = typeSystem {
            parametricType(NoAttributes, "Num") {
                parameter("a")
            }
            type(NoAttributes, "Int")
            type(NoAttributes, "Integer") {
                parametricType("Num") {
                    type("Int")
                }
            }
        }.value
        val context = SubstitutionContext(checker)
        val p1 = ParametricType(
            typeSystem.handle,
            NoAttributes,
            Alias(typeSystem.handle, "Num"),
            listOf(Parameter(typeSystem.handle, "a"))
        )
        val p2 = Alias(typeSystem.handle, "Integer")
        context.extract(Location.NoProvided, p1, p2).shouldBeRight(true)
        context.substitute(Location.NoProvided, p1, Concrete(typeSystem.handle, NoAttributes, "Int"))
            .shouldBeRight(
                ParametricType(
                    typeSystem.handle,
                    NoAttributes,
                    Alias(typeSystem.handle, "Num"),
                    listOf(Alias(typeSystem.handle, "Int"))
                )
            )
    }
})
