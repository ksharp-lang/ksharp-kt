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
import org.ksharp.typesystem.attributes.nameAttribute
import org.ksharp.typesystem.types.ImplType

private fun String.getFirstAbstraction() =
    toCodeModule()
        .artifact
        .abstractions
        .first()

private fun createSpec(description: String, code: String, expected: IrNode) =
    Triple(description, code, expected)

private fun arithmeticExpected(factory: BinaryOperationFactory) =
    factory(
        setOf(CommonAttribute.Constant, CommonAttribute.Pure),
        IrInteger(
            1,
            Location(Line(1) to Offset(5), Line(1) to Offset(6))
        ),
        IrInteger(
            2,
            Location(Line(1) to Offset(9), Line(1) to Offset(10))
        ),
        Location(Line(1) to Offset(7), Line(1) to Offset(8))
    )

class AbstractionToIrSymbolTest : StringSpec({
    val moduleInfo = preludeModule
    val loaderIrModule = LoadIrModuleFn { null }
    val functionLookup = FunctionLookup { _, _, _ -> throw RuntimeException("Not Supported") }
    val partialState = PartialIrState(
        "test",
        moduleInfo,
        loaderIrModule,
        functionLookup,
    )
    listOf(
        createSpec(
            "IrInteger expression", "fn = 10", IrInteger(
                10,
                Location(Line(1) to Offset(5), Line(1) to Offset(7))
            )
        ),
        createSpec(
            "IrDecimal expression", "fn = 10.0", IrDecimal(
                10.0,
                Location(Line(1) to Offset(5), Line(1) to Offset(9))
            )
        ),
        createSpec(
            "IrCharacter expression", "fn = 'a'", IrCharacter(
                'a',
                Location(Line(1) to Offset(5), Line(1) to Offset(8))
            )
        ),
        createSpec(
            "IrString expression", "fn = \"Hello\"", IrString(
                "Hello",
                Location(Line(1) to Offset(5), Line(1) to Offset(12))
            )
        ),
        createSpec(
            "IrList expression", "fn = [1, 2, 3]", IrList(
                setOf(CommonAttribute.Constant, CommonAttribute.Pure),
                listOf(
                    IrInteger(
                        1,
                        Location(Line(1) to Offset(6), Line(1) to Offset(7))
                    ),
                    IrInteger(
                        2,
                        Location(Line(1) to Offset(9), Line(1) to Offset(10))
                    ),
                    IrInteger(
                        3,
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
                        Location(Line(1) to Offset(7), Line(1) to Offset(8))
                    ),
                    IrInteger(
                        2,
                        Location(Line(1) to Offset(10), Line(1) to Offset(11))
                    ),
                    IrInteger(
                        3,
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
                            Location(Line(1) to Offset(6), Line(1) to Offset(12))
                        ),
                        IrInteger(
                            1,
                            Location(Line(1) to Offset(14), Line(1) to Offset(15))
                        ),
                        Location(Line(1) to Offset(6), Line(1) to Offset(12))
                    ),
                    IrPair(
                        setOf(CommonAttribute.Constant, CommonAttribute.Pure),
                        IrString(
                            "key2",
                            Location(Line(1) to Offset(17), Line(1) to Offset(23))
                        ),
                        IrInteger(
                            2,
                            Location(Line(1) to Offset(25), Line(1) to Offset(26))
                        ),
                        Location(Line(1) to Offset(17), Line(1) to Offset(23))
                    )
                ),
                Location(Line(1) to Offset(5), Line(1) to Offset(6))
            )
        ),
        createSpec(
            "IrSum expression", """fn = 1 + 2""", arithmeticExpected(::IrSum)
        ),
        createSpec(
            "IrSub expression", """fn = 1 - 2""", arithmeticExpected(::IrSub)
        ),
        createSpec(
            "IrMul expression", """fn = 1 * 2""", arithmeticExpected(::IrMul)
        ),
        createSpec(
            "IrDiv expression", """fn = 1 / 2""", arithmeticExpected(::IrDiv)
        ),
        createSpec(
            "IrMod expression", """fn = 1 % 2""", arithmeticExpected(::IrMod)
        ),
        createSpec(
            "IrPow expression", """fn = 1 ** 2""", IrPow(
                setOf(CommonAttribute.Constant, CommonAttribute.Pure),
                IrInteger(
                    1,
                    Location(Line(1) to Offset(5), Line(1) to Offset(6))
                ),
                IrInteger(
                    2,
                    Location(Line(1) to Offset(10), Line(1) to Offset(11))
                ),
                Location(Line(1) to Offset(7), Line(1) to Offset(9))
            )
        ),
        createSpec(
            "Constant IrCall expression",
            """
                    fn = sum 1 2
                    
                    sum a b = a + b
                """.trimIndent(), IrCall(
                setOf(CommonAttribute.Constant, CommonAttribute.Pure),
                null,
                CallScope("sum/2", "Num", "prelude::num"),
                listOf(
                    IrInteger(
                        1,
                        Location(Line(1) to Offset(9), Line(1) to Offset(10))
                    ),
                    IrInteger(
                        2,
                        Location(Line(1) to Offset(11), Line(1) to Offset(12))
                    )
                ),
                ImplType(
                    preludeModule.typeSystem["Num"].valueOrNull!!.cast(),
                    preludeModule.typeSystem["Long"].valueOrNull!!
                ),
                Location(Line(1) to Offset(5), Line(1) to Offset(8))
            )
        ),
        createSpec(
            "Cast expression",
            "fn = byte(10)".trimIndent(), IrNumCast(
                IrInteger(
                    10,
                    Location(Line(1) to Offset(10), Line(1) to Offset(12))
                ),
                CastType.Byte,
                Location(Line(1) to Offset(5), Line(1) to Offset(9))
            )
        ),
        createSpec(
            "If expression",
            """
                    fn = if True
                         then 10
                         else 20
                """.trimIndent(), IrIf(
                setOf(CommonAttribute.Constant, CommonAttribute.Pure),
                condition = IrBool(
                    true,
                    Location(
                        (Line(value = 1) to Offset(value = 8)),
                        (Line(value = 1) to Offset(value = 12))
                    )
                ),
                thenExpr = IrInteger(
                    10,
                    Location(
                        (Line(value = 2) to Offset(value = 10)),
                        (Line(value = 2) to Offset(value = 12))
                    )
                ),
                elseExpr = IrInteger(
                    20,
                    Location(
                        (Line(value = 3) to Offset(value = 10)),
                        (Line(value = 3) to Offset(value = 12))
                    )
                ),
                Location((Line(value = 1) to Offset(value = 5)), (Line(value = 1) to Offset(value = 7)))
            )
        ),
        createSpec(
            "Let expression, with simple bindings",
            """
                    fn = let x = 10
                             y = 20
                         then x + y
                """.trimIndent(),
            IrLet(
                attributes = setOf(CommonAttribute.Constant, CommonAttribute.Pure),
                expressions = listOf(
                    IrSetVar(
                        attributes = setOf(CommonAttribute.Constant),
                        index = 0,
                        value = IrInteger(
                            value = 10,
                            location = Location(
                                start = (Line(value = 1) to Offset(value = 13)),
                                end = (Line(value = 1) to Offset(value = 15))
                            )
                        ),
                        location = Location(
                            start = (Line(value = 1) to Offset(value = 11)),
                            end = (Line(value = 1) to Offset(value = 12))
                        )
                    ),
                    IrSetVar(
                        attributes = setOf(CommonAttribute.Constant), index = 1,
                        value = IrInteger(
                            value = 20,
                            location = Location(
                                start = (Line(value = 2) to Offset(value = 13)),
                                end = (Line(value = 2) to Offset(value = 15))
                            )
                        ),
                        location = Location(
                            start = (Line(value = 2) to Offset(value = 11)),
                            end = (Line(value = 2) to Offset(value = 12))
                        )
                    ),
                    IrSum(
                        attributes = setOf(CommonAttribute.Constant, CommonAttribute.Pure),
                        left = IrVar(
                            attributes = setOf(CommonAttribute.Constant), index = 0,
                            location = Location(
                                start = (Line(value = 3) to Offset(value = 10)),
                                end = (Line(value = 3) to Offset(value = 11))
                            )
                        ),
                        right = IrVar(
                            attributes = setOf(CommonAttribute.Constant), index = 1,
                            location = Location(
                                start = (Line(value = 3) to Offset(value = 14)),
                                end = (Line(value = 3) to Offset(value = 15))
                            )
                        ),
                        location = Location(
                            start = (Line(value = 3) to Offset(value = 12)),
                            end = (Line(value = 3) to Offset(value = 13))
                        )
                    )
                ),
                location = Location(
                    start = (Line(value = 1) to Offset(value = 5)),
                    end = (Line(value = 1) to Offset(value = 8))
                )
            )
        ),
    ).forEach { (description, code, expected) ->
        description {
            code.getFirstAbstraction()
                .abstractionToIrSymbol(partialState)
                .expr
                .shouldBe(expected)
        }
    }
    "irFunction without arguments" {
        "ten = 10"
            .getFirstAbstraction()
            .toIrSymbol(
                IrState(
                    "test",
                    moduleInfo, { null },
                    { _, _, _ -> throw RuntimeException("Not supported") },
                    mutableVariableIndexes(emptyVariableIndex)
                )
            )
            .shouldBe(
                IrFunction(
                    setOf(CommonAttribute.Internal, CommonAttribute.Constant),
                    "ten/0",
                    listOf(),
                    0,
                    IrInteger(
                        10,
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
            .toIrSymbol(
                IrState(
                    "test",
                    moduleInfo,
                    { null },
                    functionLookup,
                    mutableVariableIndexes(emptyVariableIndex)
                )
            )
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
        """
            c :: Char -> Char
            c a = a
        """.trimIndent()
            .getFirstAbstraction()
            .toIrSymbol(
                IrState(
                    "test",
                    moduleInfo,
                    { null },
                    functionLookup,
                    mutableVariableIndexes(emptyVariableIndex)
                )
            )
            .apply {
                shouldBe(
                    IrFunction(
                        setOf(CommonAttribute.Internal),
                        "c/1",
                        listOf("a"),
                        0,
                        IrArg(
                            setOf(CommonAttribute.Pure),
                            NoCaptured,
                            0,
                            Location(Line(2) to Offset(6), Line(2) to Offset(7))
                        ),
                        Location(Line(2) to Offset(0), Line(2) to Offset(1))
                    )
                )
            }
    }
})


class CustomAbstractionToIrSymbolTest : StringSpec({
    val functionLookup = FunctionLookup { _, _, _ -> throw RuntimeException("Not Supported") }
    val partialState = PartialIrState(
        "test",
        preludeModule,
        { null },
        functionLookup,
    )
    "Check a custom spec" {
        createSpec(
            "Constant IrCall expression",
            """
                    fn = sum 1 2
                    
                    sum a b = a + b
                """.trimIndent(), IrCall(
                setOf(CommonAttribute.Constant, CommonAttribute.Pure),
                null,
                CallScope("sum/2", "Num", "prelude::num"),
                listOf(
                    IrInteger(
                        1,
                        Location(Line(1) to Offset(9), Line(1) to Offset(10))
                    ),
                    IrInteger(
                        2,
                        Location(Line(1) to Offset(11), Line(1) to Offset(12))
                    )
                ),
                ImplType(
                    preludeModule.typeSystem["Num"].valueOrNull!!.cast(),
                    preludeModule.typeSystem["Long"].valueOrNull!!
                ),
                Location(Line(1) to Offset(5), Line(1) to Offset(8))
            )
        ).let { (_, code, expected) ->
            code.getFirstAbstraction()
                .abstractionToIrSymbol(partialState)
                .expr
                .shouldBe(expected)
        }
    }
})
