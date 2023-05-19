package org.ksharp.module.prelude

import org.ksharp.common.Either
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.module.prelude.types.charType
import org.ksharp.module.prelude.types.numeric
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.typeSystem
import org.ksharp.typesystem.types.*

private fun TypeSystemBuilder.number(alias: String, type: Numeric) =
    alias(TypeVisibility.Public, alias) {
        parametricType("Num") {
            numeric(type)
        }
    }

private fun createPreludeTypeSystem() = typeSystem {
    type(TypeVisibility.Public, "Unit")
    alias(TypeVisibility.Public, "Char") { Either.Right(charType) }
    parametricType(TypeVisibility.Public, "Num") {
        parameter("a")
    }

    number("Byte", Numeric.Byte)
    number("Short", Numeric.Short)
    number("Int", Numeric.Int)
    number("Long", Numeric.Long)
    number("BigInt", Numeric.BigInt)
    number("Float", Numeric.Float)
    number("Double", Numeric.Double)
    number("BigDecimal", Numeric.BigDecimal)

    parametricType(TypeVisibility.Public, "List") {
        parameter("v")
    }
    parametricType(TypeVisibility.Public, "Set") {
        parameter("v")
    }
    parametricType(TypeVisibility.Public, "Map") {
        parameter("k")
        parameter("v")
    }

    alias(TypeVisibility.Public, "String") {
        parametricType("List") {
            type("Char")
        }
    }

    alias(TypeVisibility.Public, "Bool") {
        unionType {
            clazz("True")
            clazz("False")
        }
    }
}

internal val preludeTypeSystem = createPreludeTypeSystem()