package org.ksharp.ir.transform

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset
import org.ksharp.ir.ConstantExpression
import org.ksharp.ir.Function
import org.ksharp.ir.toSemanticModuleInfo
import org.ksharp.module.prelude.preludeModule
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.toFunctionType

private fun String.getFirstAbstraction() =
    toSemanticModuleInfo()
        .abstractions
        .first()

class AbstractionToIrSymbolTest : StringSpec({
    val byteType = preludeModule.typeSystem["Byte"].valueOrNull!!
    val unitType = preludeModule.typeSystem["Unit"].valueOrNull!!
    "Constant function to irFunction" {
        "ten = 10"
            .getFirstAbstraction()
            .toIrSymbol()
            .shouldBe(
                Function(
                    setOf(CommonAttribute.Internal, CommonAttribute.Constant),
                    "ten",
                    listOf(),
                    listOf(unitType, byteType).toFunctionType(NoAttributes),
                    ConstantExpression(
                        10.toLong(),
                        byteType,
                        Location(Line(1) to Offset(6), Line(1) to Offset(8))
                    ),
                    Location(Line(1) to Offset(0), Line(1) to Offset(3))
                )
            )
    }
})
