package ksharp.nodes.prelude.types

import io.kotest.core.spec.style.StringSpec
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.get

class PreludeTypeSystemTest : StringSpec({
    "Check prelude type system" {
        preludeTypeSystem.apply {
            get("Char").map { it.representation }.shouldBeRight("char<Char>")
            get("Byte").map { it.toString() }.shouldBeRight("Num numeric<Byte>")
            get("Short").map { it.toString() }.shouldBeRight("Num numeric<Short>")
            get("Int").map { it.toString() }.shouldBeRight("Num numeric<Int>")
            get("Long").map { it.toString() }.shouldBeRight("Num numeric<Long>")
            get("BigInt").map { it.toString() }.shouldBeRight("Num numeric<BigInt>")
            get("Float").map { it.toString() }.shouldBeRight("Num numeric<Float>")
            get("Double").map { it.toString() }.shouldBeRight("Num numeric<Double>")
            get("BigDecimal").map { it.toString() }.shouldBeRight("Num numeric<BigDecimal>")
            get("String").map { it.toString() }.shouldBeRight("List Char")
            get("Num").map { it.toString() }.shouldBeRight("Num a")
            get("List").map { it.toString() }.shouldBeRight("List v")
            get("Set").map { it.toString() }.shouldBeRight("Set v")
            get("Map").map { it.toString() }.shouldBeRight("Map k v")
        }
    }
})