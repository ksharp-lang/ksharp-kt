package org.ksharp.module.prelude

import org.ksharp.common.Either
import org.ksharp.module.prelude.serializer.TypeSerializers
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.module.prelude.types.charType
import org.ksharp.module.prelude.types.numeric
import org.ksharp.typesystem.PartialTypeSystem
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.attributes.nameAttribute
import org.ksharp.typesystem.serializer.registerCatalog
import org.ksharp.typesystem.typeSystem
import org.ksharp.typesystem.types.TraitTypeFactory
import org.ksharp.typesystem.types.parametricType
import org.ksharp.typesystem.types.trait
import org.ksharp.typesystem.types.type

private fun TypeSystemBuilder.number(type: Numeric) =
    type(setOf(nameAttribute(mapOf("ir" to "num"))), type.name) {
        numeric(type)
    }

private fun TraitTypeFactory.binaryOp(name: String) =
    method(name, true) {
        parameter("a")
        parameter("a")
        parameter("a")
    }

private fun createKernelTypeSystem() = typeSystem {
    registerCatalog("prelude") {
        TypeSerializers.entries[it]
    }
    trait(setOf(nameAttribute(mapOf("ir" to "prelude::num"))), "Num", "a") {
        binaryOp("(+)")
        binaryOp("(-)")
        binaryOp("(*)")
        binaryOp("(/)")
        binaryOp("(%)")
        binaryOp("(**)")
    }
    type(NoAttributes, "Unit")
    type(NoAttributes, "Char") { Either.Right(charType) }
    parametricType(setOf(nameAttribute(mapOf("ir" to "num"))), "Num") {
        parameter("a")
    }
    number(Numeric.Byte)
    number(Numeric.Short)
    number(Numeric.Int)
    number(Numeric.Long)
    number(Numeric.BigInt)
    number(Numeric.Float)
    number(Numeric.Double)
    number(Numeric.BigDecimal)
}

internal val kernelTypeSystem = createKernelTypeSystem()

internal val preludeTypeSystem = PartialTypeSystem(preludeModule.typeSystem, emptyList())
