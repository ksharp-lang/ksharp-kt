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
    }.let {
        it.errors.shouldBeEmpty()
        it.value
    }
    "solve param" {
        val param = newParameter()
        ts.solve(param).shouldBeRight(param)
    }
    "solve concrete type" {
        val type = Concrete(NoAttributes, "Int")
        ts.solve(type).shouldBeRight(type)
    }
    "solve parametric type" {
        val type = ts["StringList"].valueOrNull.shouldNotBeNull()
        ts.solve(type).also { println(it) }
            .shouldBeRight(ParametricType(NoAttributes, Alias("List"), listOf(Concrete(NoAttributes, "String"))))
    }
    "solve function type" {
        val type = ts["Sum"].valueOrNull.shouldNotBeNull()
        ts.solve(type).also { println(it) }
            .shouldBeRight(
                FunctionType(
                    NoAttributes, listOf(
                        ParametricType(NoAttributes, Alias("List"), listOf(Concrete(NoAttributes, "String"))),
                        Concrete(NoAttributes, "String")
                    )
                )
            )
    }
})
