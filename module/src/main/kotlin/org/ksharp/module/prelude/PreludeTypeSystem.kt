package org.ksharp.module.prelude

import org.ksharp.common.Either
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.module.prelude.types.charType
import org.ksharp.module.prelude.types.numeric
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.typeSystem
import org.ksharp.typesystem.types.alias
import org.ksharp.typesystem.types.parametricType
import org.ksharp.typesystem.types.type
import org.ksharp.typesystem.types.unionType

private fun TypeSystemBuilder.number(alias: String, type: Numeric) =
    alias(alias) {
        parametricType("Num") {
            numeric(type)
        }
    }

private fun createPreludeTypeSystem() = typeSystem {
    type("Unit")
    alias("Char") { Either.Right(charType) }
    parametricType("Num") {
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

    parametricType("List") {
        parameter("v")
    }
    parametricType("Set") {
        parameter("v")
    }
    parametricType("Map") {
        parameter("k")
        parameter("v")
    }

    alias("String") {
        parametricType("List") {
            type("Char")
        }
    }

    alias("Bool") {
        unionType {
            clazz("True")
            clazz("False")
        }
    }
}

internal val preludeTypeSystem = createPreludeTypeSystem()