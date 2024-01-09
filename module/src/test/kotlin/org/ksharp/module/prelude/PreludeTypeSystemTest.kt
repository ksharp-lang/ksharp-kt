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

private fun PartialTypeSystem.repr(name: String): ErrorOrValue<String> =
    this[name].flatMap {
        it().flatMap {
            Either.Right(it.toString())
        }
    }

class PreludeTypeSystemTest : StringSpec({
    "Check prelude type system" {
        preludeTypeSystem.apply {
            repr("Char").shouldBeRight("Char")
            repr("Byte").shouldBeRight("Byte")
            repr("Short").shouldBeRight("Short")
            repr("Int").shouldBeRight("Int")
            repr("Long").shouldBeRight("Long")
            repr("BigInt").shouldBeRight("BigInt")
            repr("Float").shouldBeRight("Float")
            repr("Double").shouldBeRight("Double")
            repr("BigDecimal").shouldBeRight("BigDecimal")
            repr("String").shouldBeRight("String")
            repr("Num").shouldBeRight(
                """
                trait Num a =
                    (+) :: a -> a -> a
                    (-) :: a -> a -> a
                    (*) :: a -> a -> a
                    ( :: a -> a -> a
                    (%) :: a -> a -> a
                    (**) :: a -> a -> a
            """.trimIndent()
            )
            repr("List").shouldBeRight("List v")
            repr("Set").shouldBeRight("Set v")
            repr("Map").shouldBeRight("Map k v")
            repr("Unit").shouldBeRight("Unit")
            repr("Bool").shouldBeRight("True\n|False")
        }
    }
    "Check charType members" {
        charType.apply {
            compound.shouldBeFalse()
            terms.shouldBeEmpty()
            representation.shouldBe("Char")
        }
    }
    "Check numeric types" {
        NumericType(Numeric.Int).apply {
            compound.shouldBeFalse()
            terms.shouldBeEmpty()
            representation.shouldBe("Int")
        }
    }
})
