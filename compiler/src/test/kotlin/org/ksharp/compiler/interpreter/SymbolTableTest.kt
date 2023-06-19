package org.ksharp.compiler.interpreter

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class SymbolTableTest : StringSpec({
    "Readonly symbol table" {
        readOnlySymbolTable(
            mapOf(
                "a" to 10,
                "b" to 2
            )
        ).apply {
            this["a"].shouldBe(10)
            this["b"].shouldBe(2)
            this["c"].shouldBeNull()
        }
    }
    "Dynamic symbol table" {
        openSymbolTable().apply {
            set("a", 10).shouldBeTrue()
            set("b", 2).shouldBeTrue()
            set("a", 5).shouldBeFalse()
        }.apply {
            this["a"].shouldBe(10)
            this["b"].shouldBe(2)
            this["c"].shouldBeNull()
        }
    }
    "Chain symbol tables" {
        val dynamic = openSymbolTable()
        chainSymbolTables(
            dynamic,
            readOnlySymbolTable(
                mapOf(
                    "a" to 10,
                    "b" to 2
                )
            ),
        ).apply {
            this["b"].shouldBe(2)
            dynamic.set("b", 5).shouldBeTrue()
            this["b"].shouldBe(5)
            this["c"].shouldBeNull()
        }
    }
})
