package org.ksharp.ir.transform

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset
import org.ksharp.common.cast
import org.ksharp.ir.*
import org.ksharp.module.prelude.preludeModule
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NameAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.attributes.nameAttribute
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
            "IrInteger expression", "fn = 10", IrInteger(
                10,
                byteType,
                Location(Line(1) to Offset(5), Line(1) to Offset(7))
            )
        ),
        createSpec(
            "IrDecimal expression", "fn = 10.0", IrDecimal(
                10.0,
                doubleType,
                Location(Line(1) to Offset(5), Line(1) to Offset(9))
            )
        ),
        createSpec(
            "IrCharacter expression", "fn = 'a'", IrCharacter(
                'a',
                charType,
                Location(Line(1) to Offset(5), Line(1) to Offset(8))
            )
        ),
        createSpec(
            "IrString expression", "fn = \"Hello\"", IrString(
                "Hello",
                stringType,
                Location(Line(1) to Offset(5), Line(1) to Offset(12))
            )
        ),
        createSpec(
            "IrList expression", "fn = [1, 2, 3]", IrString(
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
    "Name attribute for java" {
        """
            @name("diez" for="java")
            ten = 10
        """.trimIndent()
            .getFirstAbstraction()
            .toIrSymbol()
            .attributes
            .apply {
                shouldBe(
                    setOf(
                        CommonAttribute.Internal,
                        CommonAttribute.Constant,
                        nameAttribute(mapOf("java" to "diez"))
                    ),
                )
                first { it is NameAttribute }.cast<NameAttribute>().value.shouldBe(mapOf("java" to "diez"))
            }
    }
})
