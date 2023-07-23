package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.solver.solve
import org.ksharp.typesystem.types.*

class SolverTest : StringSpec({
    "reduce param" {
        val ts = typeSystem { }.value
        val param = newParameter()
        ts.solve(param).shouldBeRight(param)
    }
    "reduce concrete type" {
        val ts = typeSystem { }.value
        val type = Concrete(NoAttributes, "Int")
        ts.solve(type).shouldBeRight(type)
    }
    "reduce parametric type" {
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
        }.let {
            it.errors.shouldBeEmpty()
            it.value
        }
        val type = ts["StringList"].valueOrNull.shouldNotBeNull()
        ts.solve(type).also { println(it) }
            .shouldBeRight(ParametricType(NoAttributes, Alias("List"), listOf(Concrete(NoAttributes, "String"))))
    }
})
