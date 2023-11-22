package org.ksharp.semantics.scopes

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.nodes.semantic.Symbol
import org.ksharp.nodes.semantic.TypeSemanticInfo
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.types.newParameterForTesting
import org.ksharp.typesystem.types.resetParameterCounterForTesting

private fun testTypePromise(id: Int) = TypeSemanticInfo(Either.Right(newParameterForTesting(id)))


class SymbolTableTest : StringSpec({
    "Create symbol table" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", testTypePromise(1), Location.NoProvided
            ).shouldBeRight()
            register(
                "b", testTypePromise(2), Location.NoProvided
            ).shouldBeRight()
            register(
                "c", testTypePromise(3), Location.NoProvided
            ).shouldBeRight()
            register("a", testTypePromise(4), Location.NoProvided).shouldBeLeft(
                TableErrorCode.AlreadyDefined.new(Location.NoProvided, "classifier" to "Variable", "name" to "a")
            )
        }.build().apply {
            this["a"]!!.apply {
                first.shouldBe(
                    Symbol("a", testTypePromise(1))
                )
                second.shouldBe(Location.NoProvided)
            }
            this["b"]!!.apply {
                first.shouldBe(
                    Symbol("b", testTypePromise(2))
                )
                second.shouldBe(Location.NoProvided)
            }
            this["c"]!!.apply {
                first.shouldBe(
                    Symbol("c", testTypePromise(3))
                )
                second.shouldBe(Location.NoProvided)
            }
        }
    }
    "Hiding variable in nexted symbol table" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", testTypePromise(1), Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            SymbolTableBuilder(this, ErrorCollector()).apply {
                register("a", testTypePromise(2), Location.NoProvided)
            }.build().apply {
                this["a"]!!.apply {
                    first.shouldBe(
                        Symbol("a", testTypePromise(2))
                    )
                    second.shouldBe(Location.NoProvided)
                }
            }
        }
    }
    "Accessing parent symbol" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", testTypePromise(1), Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            SymbolTableBuilder(this, ErrorCollector()).apply {
                register("b", testTypePromise(2), Location.NoProvided)
            }.build().apply {
                this["a"]!!.apply {
                    first.shouldBe(
                        Symbol("a", testTypePromise(1))
                    )
                    second.shouldBe(Location.NoProvided)
                }
            }
        }
    }
    "Accessing no registered symbol" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", testTypePromise(1), Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            this["c"].shouldBeNull()
        }
    }
    "Symbol table with compound types" {
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", testTypePromise(1), Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            this["a"]!!.apply {
                first.shouldBe(
                    Symbol("a", testTypePromise(1))
                )
                second.shouldBe(Location.NoProvided)
            }
        }
    }
}) {
    override suspend fun beforeEach(testCase: TestCase) {
        super.beforeEach(testCase)
        resetParameterCounterForTesting()
    }
}
