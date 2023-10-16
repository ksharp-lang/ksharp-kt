package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.solver.solve
import org.ksharp.typesystem.types.*

class SolverTest : StringSpec({
    val ts = typeSystem {
        type(NoAttributes, "String")
        type(NoAttributes, "Str") {
            alias("String")
        }
        parametricType(NoAttributes, "List") {
            parameter("a")
        }
        type(NoAttributes, "ListAlias") {
            alias("List")
        }
        type(NoAttributes, "StringList") {
            parametricType("ListAlias") {
                type("String")
            }
        }
        type(NoAttributes, "Sum") {
            functionType {
                parametricType("ListAlias") {
                    type("String")
                }
                type("String")
            }
        }
        type(NoAttributes, "Tuple") {
            tupleType {
                type("String")
                type("String")
            }
        }
        type(NoAttributes, "Either") {
            unionType {
                clazz("Left") {
                    type("Str")
                }
                clazz("Right") {
                    type("String")
                }
            }
        }
    }.let {
        it.errors.shouldBeEmpty()
        it.value
    }
    "solve param" {
        val param = ts.newParameter()
        ts.solve(param).shouldBeRight(param)
    }
    "solve concrete type" {
        val type = Concrete(ts.handle, NoAttributes, "Int")
        ts.solve(type).shouldBeRight(type)
    }
    "solve type alias" {
        val type = TypeAlias(ts.handle, NoAttributes, "String")
        ts.solve(type).shouldBeRight(Concrete(ts.handle, NoAttributes, "String"))
    }
    "solve alias" {
        val type = Alias(ts.handle, "Str")
        ts.solve(type).shouldBeRight(Concrete(ts.handle, NoAttributes, "String"))
    }
    "solve parametric type" {
        val type = ts["StringList"].valueOrNull.shouldNotBeNull()
        ts.solve(type).also { println(it) }
            .shouldBeRight(
                ParametricType(
                    ts.handle,
                    NoAttributes,
                    Alias(ts.handle, "List"),
                    listOf(Concrete(ts.handle, NoAttributes, "String"))
                )
            )
    }
    "solve function type" {
        val type = ts["Sum"].valueOrNull.shouldNotBeNull()
        ts.solve(type).also { println(it) }
            .shouldBeRight(
                FunctionType(
                    ts.handle,
                    NoAttributes, listOf(
                        ParametricType(
                            ts.handle,
                            NoAttributes,
                            Alias(ts.handle, "List"),
                            listOf(Concrete(ts.handle, NoAttributes, "String"))
                        ),
                        Concrete(ts.handle, NoAttributes, "String")
                    )
                )
            )
    }
    "solve tuple type" {
        val type = ts["Tuple"].valueOrNull.shouldNotBeNull()
        ts.solve(type).also { println(it) }
            .shouldBeRight(
                TupleType(
                    ts.handle,
                    NoAttributes, listOf(
                        Concrete(ts.handle, NoAttributes, "String"),
                        Concrete(ts.handle, NoAttributes, "String")
                    )
                )
            )
    }
    "solve union type" {
        val type = ts["Either"].valueOrNull.shouldNotBeNull()
        ts.solve(type).also { println(it) }
            .shouldBeRight(
                UnionType(
                    ts.handle,
                    NoAttributes, mapOf(
                        "Left" to UnionType.ClassType(
                            ts.handle,
                            "Left",
                            listOf(Concrete(ts.handle, NoAttributes, "String"))
                        ),
                        "Right" to UnionType.ClassType(
                            ts.handle,
                            "Right",
                            listOf(Concrete(ts.handle, NoAttributes, "String"))
                        )
                    )
                )
            )
    }
})
