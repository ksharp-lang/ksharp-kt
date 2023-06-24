package org.ksharp.ir.transform

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset
import org.ksharp.ir.*
import org.ksharp.module.prelude.preludeModule
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.toFunctionType

private fun String.getFirstAbstraction() =
    toSemanticModuleInfo()
        .abstractions
        .first()

private fun createSpec(description: String, code: String, expected: IrNode) =
    Triple(description, code, expected)

class AbstractionToIrSymbolTest : StringSpec({
    val byteType = preludeModule.typeSystem["Byte"].valueOrNull!!
    val doubleType = preludeModule.typeSystem["Double"].valueOrNull!!
    val charType = preludeModule.typeSystem["Char"].valueOrNull!!
    val stringType = preludeModule.typeSystem["String"].valueOrNull!!
    val unitType = preludeModule.typeSystem["Unit"].valueOrNull!!
    listOf(
        createSpec(
            "IrInteger expression", "ten = 10", IrInteger(
                10,
                byteType,
                Location(Line(1) to Offset(6), Line(1) to Offset(8))
            )
        ),
        createSpec(
            "IrDecimal expression", "ten = 10.0", IrDecimal(
                10.0,
                doubleType,
                Location(Line(1) to Offset(6), Line(1) to Offset(10))
            )
        ),
        createSpec(
            "IrCharacter expression", "ten = 'a'", IrCharacter(
                'a',
                charType,
                Location(Line(1) to Offset(6), Line(1) to Offset(9))
            )
        ),
        createSpec(
            "IrString expression", "ten = \"Hello\"", IrString(
                "Hello",
                stringType,
                Location(Line(1) to Offset(6), Line(1) to Offset(13))
            )
        ),
    ).forEach { (description, code, expected) ->
        description {
            code.getFirstAbstraction()
                .toIrSymbol()
                .expr.shouldBe(expected)
        }
    }
    "irFunction without arguments" {
        "ten = 10"
            .getFirstAbstraction()
            .toIrSymbol()
            .shouldBe(
                IrFunction(
                    setOf(CommonAttribute.Internal, CommonAttribute.Constant),
                    "ten",
                    listOf(),
                    listOf(unitType, byteType).toFunctionType(NoAttributes),
                    IrInteger(
                        10,
                        byteType,
                        Location(Line(1) to Offset(6), Line(1) to Offset(8))
                    ),
                    Location(Line(1) to Offset(0), Line(1) to Offset(3))
                )
            )
    }
})
