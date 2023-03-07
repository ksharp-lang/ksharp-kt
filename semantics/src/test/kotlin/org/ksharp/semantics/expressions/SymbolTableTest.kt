package org.ksharp.semantics.expressions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.prelude.types.preludeTypeSystem
import org.ksharp.semantics.scopes.TableErrorCode
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight

class SymbolTableTest : StringSpec({
    "Create symbol table" {
        val typeSystem = preludeTypeSystem.value
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", typeSystem["Int"], Location.NoProvided
            ).shouldBeRight()
            register(
                "b", typeSystem["Long"], Location.NoProvided
            ).shouldBeRight()
            register(
                "c", typeSystem["String"], Location.NoProvided
            ).shouldBeRight()
            register("a", typeSystem["Long"], Location.NoProvided).shouldBeLeft(
                TableErrorCode.AlreadyDefined.new(Location.NoProvided, "classifier" to "Variable", "name" to "a")
            )
        }.build().apply {
            summary.shouldBe(SymbolSummary(4))
            this["a"]!!.apply {
                first.shouldBe(
                    Symbol(
                        typeSystem["Int"].valueOrNull!!,
                        0,
                        RecordSize.Single
                    )
                )
                second.shouldBe(Location.NoProvided)
            }
            this["b"]!!.apply {
                first.shouldBe(
                    Symbol(
                        typeSystem["Long"].valueOrNull!!,
                        1,
                        RecordSize.Double
                    )
                )
                second.shouldBe(Location.NoProvided)
            }
            this["c"]!!.apply {
                first.shouldBe(
                    Symbol(
                        typeSystem["String"].valueOrNull!!,
                        3,
                        RecordSize.Single
                    )
                )
                second.shouldBe(Location.NoProvided)
            }
        }
    }
    "Hiding variable in nexted symbol table" {
        val typeSystem = preludeTypeSystem.value
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", typeSystem["Int"], Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            SymbolTableBuilder(this, ErrorCollector()).apply {
                register("a", typeSystem["Long"], Location.NoProvided)
            }.build().apply {
                summary.shouldBe(SymbolSummary(3))
                this["a"]!!.apply {
                    first.shouldBe(
                        Symbol(
                            typeSystem["Long"].valueOrNull!!,
                            1,
                            RecordSize.Double
                        )
                    )
                    second.shouldBe(Location.NoProvided)
                }
            }
        }
    }
    "Accessing parent symbol" {
        val typeSystem = preludeTypeSystem.value
        SymbolTableBuilder(null, ErrorCollector()).apply {
            register(
                "a", typeSystem["Int"], Location.NoProvided
            ).shouldBeRight()
        }.build().apply {
            SymbolTableBuilder(this, ErrorCollector()).apply {
                register("b", typeSystem["Long"], Location.NoProvided)
            }.build().apply {
                summary.shouldBe(SymbolSummary(3))
                this["a"]!!.apply {
                    first.shouldBe(
                        Symbol(
                            typeSystem["Int"].valueOrNull!!,
                            0,
                            RecordSize.Single
                        )
                    )
                    second.shouldBe(Location.NoProvided)
                }
            }
        }
    }
})