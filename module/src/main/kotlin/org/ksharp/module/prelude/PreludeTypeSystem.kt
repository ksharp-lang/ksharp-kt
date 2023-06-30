package org.ksharp.module.prelude

import org.ksharp.common.Either
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.module.prelude.types.charType
import org.ksharp.module.prelude.types.numeric
import org.ksharp.typesystem.PartialTypeSystem
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.typeSystem
import org.ksharp.typesystem.types.parametricType
import org.ksharp.typesystem.types.type

private fun TypeSystemBuilder.number(alias: String, type: Numeric) =
    type(NoAttributes, alias) {
        parametricType("Num") {
            numeric(type)
        }
    }

private fun createKernelTypeSystem() = typeSystem {
    type(NoAttributes, "Unit")
    type(NoAttributes, "Char") { Either.Right(charType) }
    parametricType(NoAttributes, "Num") {
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
}

internal val kernelTypeSystem = createKernelTypeSystem()

internal val preludeTypeSystem = PartialTypeSystem(preludeModule.typeSystem, emptyList())
