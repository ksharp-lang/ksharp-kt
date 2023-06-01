package org.ksharp.module.prelude

import org.ksharp.common.Either
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.module.prelude.types.charType
import org.ksharp.module.prelude.types.numeric
import org.ksharp.typesystem.PartialTypeSystem
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.typeSystem
import org.ksharp.typesystem.types.TypeVisibility
import org.ksharp.typesystem.types.alias
import org.ksharp.typesystem.types.type

private fun TypeSystemBuilder.number(alias: String, type: Numeric) =
    alias(TypeVisibility.Public, alias) {
        numeric(type)
    }

private fun createKernelTypeSystem() = typeSystem {
    type(TypeVisibility.Public, "KernelUnit")
    alias(TypeVisibility.Public, "KernelChar") { Either.Right(charType) }
    number("NativeByte", Numeric.Byte)
    number("NativeShort", Numeric.Short)
    number("NativeInt", Numeric.Int)
    number("NativeLong", Numeric.Long)
    number("NativeBigInt", Numeric.BigInt)
    number("NativeFloat", Numeric.Float)
    number("NativeDouble", Numeric.Double)
    number("NativeBigDecimal", Numeric.BigDecimal)
}

internal val kernelTypeSystem = createKernelTypeSystem()

internal val preludeTypeSystem = PartialTypeSystem(preludeModule.typeSystem, emptyList())
