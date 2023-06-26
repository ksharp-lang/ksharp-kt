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
import org.ksharp.typesystem.types.alias
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
            "IrList expression", "fn = [1, 2, 3]", IrList(
                setOf(CommonAttribute.Constant, CommonAttribute.Pure),
                listOf(
                    IrInteger(
                        1,
                        byteType,
                        Location(Line(1) to Offset(6), Line(1) to Offset(7))
                    ),
                    IrInteger(
                        2,
                        byteType,
                        Location(Line(1) to Offset(9), Line(1) to Offset(10))
                    ),
                    IrInteger(
                        3,
                        byteType,
                        Location(Line(1) to Offset(12), Line(1) to Offset(13))
                    )
                ),
                Location(Line(1) to Offset(5), Line(1) to Offset(6))
            )
        ),
        createSpec(
            "IrSet expression", "fn = #[1, 2, 3]", IrSet(
                setOf(CommonAttribute.Constant, CommonAttribute.Pure),
                listOf(
                    IrInteger(
                        1,
                        byteType,
                        Location(Line(1) to Offset(7), Line(1) to Offset(8))
                    ),
                    IrInteger(
                        2,
                        byteType,
                        Location(Line(1) to Offset(10), Line(1) to Offset(11))
                    ),
                    IrInteger(
                        3,
                        byteType,
                        Location(Line(1) to Offset(13), Line(1) to Offset(14))
                    )
                ),
                Location(Line(1) to Offset(5), Line(1) to Offset(7))
            )
        ),
        createSpec(
            "IrMap expression", """fn = {"key1": 1, "key2": 2}""", IrMap(
                setOf(CommonAttribute.Constant, CommonAttribute.Pure),
                listOf(
                    IrPair(
                        setOf(CommonAttribute.Constant, CommonAttribute.Pure),
                        IrString(
                            "key1",
                            stringType,
                            Location(Line(1) to Offset(6), Line(1) to Offset(12))
                        ),
                        IrInteger(
                            1,
                            byteType,
                            Location(Line(1) to Offset(14), Line(1) to Offset(15))
                        ),
                        Location(Line(1) to Offset(6), Line(1) to Offset(12))
                    ),
                    IrPair(
                        setOf(CommonAttribute.Constant, CommonAttribute.Pure),
                        IrString(
                            "key2",
                            stringType,
                            Location(Line(1) to Offset(17), Line(1) to Offset(23))
                        ),
                        IrInteger(
                            2,
                            byteType,
                            Location(Line(1) to Offset(25), Line(1) to Offset(26))
                        ),
                        Location(Line(1) to Offset(17), Line(1) to Offset(23))
                    )
                ),
                Location(Line(1) to Offset(5), Line(1) to Offset(6))
            )
        ),
    ).forEach { (description, code, expected) ->
        description {
            code.getFirstAbstraction()
                .toIrSymbol(emptyVariableIndex)
                .expr.shouldBe(expected)
        }
    }
    "irFunction without arguments" {
        "ten = 10"
            .getFirstAbstraction()
            .toIrSymbol(emptyVariableIndex)
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
            .toIrSymbol(emptyVariableIndex)
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
    "Function with arguments" {
        val internalCharType =
            preludeModule.typeSystem.alias("KernelChar").valueOrNull!!
        """
            c :: KernelChar -> KernelChar
            c a = a
        """.trimIndent()
            .getFirstAbstraction()
            .toIrSymbol(emptyVariableIndex)
            .apply {
                shouldBe(
                    IrFunction(
                        setOf(CommonAttribute.Internal),
                        "c",
                        listOf("a"),
                        listOf(internalCharType, internalCharType).toFunctionType(NoAttributes),
                        IrVar(
                            setOf(CommonAttribute.Pure),
                            0,
                            Location(Line(2) to Offset(6), Line(2) to Offset(7))
                        ),
                        Location(Line(2) to Offset(0), Line(2) to Offset(1))
                    )
                )
            }
    }
})