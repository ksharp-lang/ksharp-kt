package org.ksharp.semantics.expressions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.TypePromise
import org.ksharp.semantics.scopes.TableErrorCode
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight

private data class TestTypePromise(val value: String) : TypePromise

class SymbolTableTest : StringSpec({
    "Create symbol table" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", TestTypePromise("Int"), Location.NoProvided
            ).shouldBeRight()
            register(
                "b", TestTypePromise("Long"), Location.NoProvided
            ).shouldBeRight()
            register(
                "c", TestTypePromise("String"), Location.NoProvided
            ).shouldBeRight()
            register("a", TestTypePromise("Long"), Location.NoProvided).shouldBeLeft(
                TableErrorCode.AlreadyDefined.new(Location.NoProvided, "classifier" to "Variable", "name" to "a")
            )
        }.build().apply {
            this["a"]!!.apply {
                first.shouldBe(
                    Symbol(TestTypePromise("Int"))
                )
                second.shouldBe(Location.NoProvided)
            }
            this["b"]!!.apply {
                first.shouldBe(
                    Symbol(TestTypePromise("Long"))
                )
                second.shouldBe(Location.NoProvided)
            }
            this["c"]!!.apply {
                first.shouldBe(
                    Symbol(TestTypePromise("String"))
                )
                second.shouldBe(Location.NoProvided)
            }
        }
    }
    "Hiding variable in nexted symbol table" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", TestTypePromise("Int"), Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            SymbolTableBuilder(this, ErrorCollector()).apply {
                register("a", TestTypePromise("Long"), Location.NoProvided)
            }.build().apply {
                this["a"]!!.apply {
                    first.shouldBe(
                        Symbol(TestTypePromise("Long"))
                    )
                    second.shouldBe(Location.NoProvided)
                }
            }
        }
    }
    "Accessing parent symbol" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", TestTypePromise("Int"), Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            SymbolTableBuilder(this, ErrorCollector()).apply {
                register("b", TestTypePromise("Long"), Location.NoProvided)
            }.build().apply {
                this["a"]!!.apply {
                    first.shouldBe(
                        Symbol(TestTypePromise("Int"))
                    )
                    second.shouldBe(Location.NoProvided)
                }
            }
        }
    }
    "Accessing no registered symbol" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", TestTypePromise("Int"), Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            this["c"].shouldBeNull()
        }
    }
    "Symbol table with compound types" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", TestTypePromise("KeyMap"), Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            this["a"]!!.apply {
                first.shouldBe(
                    Symbol(TestTypePromise("KeyMap"))
                )
                second.shouldBe(Location.NoProvided)
            }
        }
    }
})