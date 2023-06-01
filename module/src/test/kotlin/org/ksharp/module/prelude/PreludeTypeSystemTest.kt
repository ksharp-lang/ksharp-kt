package org.ksharp.module.prelude

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.sequences.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.common.Either
import org.ksharp.common.ErrorOrValue
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.module.prelude.types.NumericType
import org.ksharp.module.prelude.types.charType
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.PartialTypeSystem
import org.ksharp.typesystem.get
import org.ksharp.typesystem.invoke

private fun PartialTypeSystem.repr(name: String): ErrorOrValue<String> =
    this[name].flatMap {
        this(it).flatMap {
            Either.Right(it.toString())
        }
    }

class PreludeTypeSystemTest : StringSpec({
    "Check prelude type system" {
        preludeTypeSystem.apply {
            repr("NativeByte").shouldBeRight("numeric<Byte>")
            repr("NativeShort").shouldBeRight("numeric<Short>")
            repr("NativeInt").shouldBeRight("numeric<Int>")
            repr("NativeLong").shouldBeRight("numeric<Long>")
            repr("NativeBigInt").shouldBeRight("numeric<BigInt>")
            repr("NativeFloat").shouldBeRight("numeric<Float>")
            repr("NativeDouble").shouldBeRight("numeric<Double>")
            repr("NativeBigDecimal").shouldBeRight("numeric<BigDecimal>")
            repr("Char").shouldBeRight("char<Char>")
            repr("Byte").shouldBeRight("Num NativeByte")
            repr("Short").shouldBeRight("Num NativeShort")
            repr("Int").shouldBeRight("Num NativeInt")
            repr("Long").shouldBeRight("Num NativeLong")
            repr("BigInt").shouldBeRight("Num NativeBigInt")
            repr("Float").shouldBeRight("Num NativeFloat")
            repr("Double").shouldBeRight("Num NativeDouble")
            repr("BigDecimal").shouldBeRight("Num NativeBigDecimal")
            repr("String").shouldBeRight("List Char")
            repr("Num").shouldBeRight("Num a")
            repr("List").shouldBeRight("List v")
            repr("Set").shouldBeRight("Set v")
            repr("Map").shouldBeRight("Map k v")
            repr("Unit").shouldBeRight("KernelUnit")
            repr("Bool").shouldBeRight("True\n|False")
        }
    }
    "Check charType members" {
        charType.apply {
            compound.shouldBeFalse()
            terms.shouldBeEmpty()
            representation.shouldBe("char<Char>")
        }
    }
    "Check numeric types" {
        NumericType(Numeric.Int).apply {
            compound.shouldBeFalse()
            terms.shouldBeEmpty()
            representation.shouldBe("numeric<Int>")
        }
    }
})
