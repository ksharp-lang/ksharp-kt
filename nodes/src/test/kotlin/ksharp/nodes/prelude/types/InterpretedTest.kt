package ksharp.nodes.prelude.types

import io.kotest.core.spec.style.StringSpec
import ksharp.test.shouldBeRight
import org.ksharp.typesystem.get
import org.ksharp.typesystem.typeSystem
import org.ksharp.typesystem.types.alias
import org.ksharp.typesystem.types.parametricType

class InterpretedTest : StringSpec({
    "Check interpreted test" {
        typeSystem {
            alias("Int") {
                interpreted(Int::class)
            }
            parametricType("List") {
                interpreted(Int::class)
            }
        }.apply {
            get("Int").also {
                it.map { t -> t.representation }.shouldBeRight("interpreted<kotlin.Int>")
            }.shouldBeRight(Interpreted(Int::class))
            get("List").map { it.representation }.shouldBeRight("(List interpreted<kotlin.Int>)")
        }
    }
})