package org.ksharp.semantics.scopes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.ResolvedTypePromise
import org.ksharp.semantics.inference.paramTypePromise
import org.ksharp.semantics.nodes.Symbol
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.types.Parameter

private fun testTypePromise(name: String) = paramTypePromise(name)


class SymbolTableTest : StringSpec({
    "Create symbol table" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", testTypePromise("Int"), Location.NoProvided
            ).shouldBeRight()
            register(
                "b", testTypePromise("Long"), Location.NoProvided
            ).shouldBeRight()
            register(
                "c", testTypePromise("String"), Location.NoProvided
            ).shouldBeRight()
            register("a", testTypePromise("Long"), Location.NoProvided).shouldBeLeft(
                TableErrorCode.AlreadyDefined.new(Location.NoProvided, "classifier" to "Variable", "name" to "a")
            )
        }.build().apply {
            this["a"]!!.apply {
                first.shouldBe(
                    Symbol(testTypePromise("Int"))
                )
                second.shouldBe(Location.NoProvided)
            }
            this["b"]!!.apply {
                first.shouldBe(
                    Symbol(testTypePromise("Long"))
                )
                second.shouldBe(Location.NoProvided)
            }
            this["c"]!!.apply {
                first.shouldBe(
                    Symbol(testTypePromise("String"))
                )
                second.shouldBe(Location.NoProvided)
            }
        }
    }
    "Hiding variable in nexted symbol table" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", testTypePromise("Int"), Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            SymbolTableBuilder(this, ErrorCollector()).apply {
                register("a", testTypePromise("Long"), Location.NoProvided)
            }.build().apply {
                this["a"]!!.apply {
                    first.shouldBe(
                        Symbol(testTypePromise("Long"))
                    )
                    second.shouldBe(Location.NoProvided)
                }
            }
        }
    }
    "Accessing parent symbol" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", testTypePromise("Int"), Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            SymbolTableBuilder(this, ErrorCollector()).apply {
                register("b", testTypePromise("Long"), Location.NoProvided)
            }.build().apply {
                this["a"]!!.apply {
                    first.shouldBe(
                        Symbol(testTypePromise("Int"))
                    )
                    second.shouldBe(Location.NoProvided)
                }
            }
        }
    }
    "Accessing no registered symbol" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", testTypePromise("Int"), Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            this["c"].shouldBeNull()
        }
    }
    "Symbol table with compound types" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", testTypePromise("KeyMap"), Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            this["a"]!!.apply {
                first.shouldBe(
                    Symbol(testTypePromise("KeyMap"))
                )
                second.shouldBe(Location.NoProvided)
            }
        }
    }
})
