package org.ksharp.ir

import com.oracle.truffle.api.strings.TruffleString
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.cast
import java.math.BigDecimal
import java.math.BigInteger

private data class Call(
    val arguments: List<Any>,
    val expectedResult: Any
)

private fun String.evaluateFirstFunction(arguments: List<Any>) =
    toCodeModule()
        .toIrModule()
        .symbols
        .first {
            it is IrFunction
        }
        .cast<IrFunction>()
        .call(*arguments.toTypedArray())

private fun createSpec(description: String, code: String, expected: Any, vararg arguments: Any) =
    Triple(description, code.also(::println), Call(arguments.toList(), expected))


class EvaluateTest : StringSpec({
    listOf(
        createSpec("Integer expression", "fn = 10", 10.toLong()),
        createSpec("Decimal expression", "fn = 10.5", 10.5),
        createSpec("Character expression", "fn = 'a'", 'a'),
        createSpec(
            "String expression", "fn = \"Hello World\"",
            TruffleString.FromJavaStringNode.create().execute("Hello World", TruffleString.Encoding.UTF_16)
        ),
        createSpec(
            "List expression", "fn = [1, 2, 3]",
            listOf(1.toLong(), 2.toLong(), 3.toLong())
        ),
        createSpec(
            "List expression 2", "fn = listOf 1 2 3",
            listOf(1.toLong(), 2.toLong(), 3.toLong())
        ),
        createSpec(
            "Array expression", "fn = arrayOf 1 2 3",
            arrayOf(1.toLong(), 2.toLong(), 3.toLong())
        ),
        createSpec(
            "Set expression", "fn = #[1, 2, 3]",
            setOf(1.toLong(), 2.toLong(), 3.toLong())
        ),
        createSpec(
            "Set expression 2", "fn = setOf 1 2 3",
            setOf(1.toLong(), 2.toLong(), 3.toLong())
        ),
        createSpec(
            "Map expression", "fn = {1 : 2, 2: 4, 3: 6}",
            mapOf(1.toLong() to 2.toLong(), 2.toLong() to 4.toLong(), 3.toLong() to 6.toLong())
        ),
        createSpec(
            "Map expression 2", "fn = mapOf (pair 1 2) (pair 2 4) (pair 3 6)",
            mapOf(1.toLong() to 2.toLong(), 2.toLong() to 4.toLong(), 3.toLong() to 6.toLong())
        ),
        createSpec("Sum expression", "fn = 1 + 2", 3.toLong()),
        createSpec("Sub expression", "fn = 1 - 2", (-1).toLong()),
        createSpec("Mul expression", "fn = 2 * 3", 6.toLong()),
        createSpec("Div expression", "fn = 6 / 2", 3.toLong()),
        createSpec("Pow expression", "fn = 2 ** 3", 8.toLong()),
        createSpec("Mod expression", "fn = 7 % 2", 1.toLong()),

        createSpec("< expression", "fn = 1 < 2", true),
        createSpec("<= expression", "fn = 2 <= 1", false),
        createSpec("> expression", "fn = 2 > 3", false),
        createSpec(">= expression", "fn = 6 >= 2", true),
        createSpec("== numeric expression", "fn = 2 == 2", true),
        createSpec("not == numeric expression", "fn = 7 != 7", false),

        createSpec("== object expression", "fn = \"Hola\" == \"Mundo\"", false),
        createSpec("not == object expression", "fn = \"Hola\" != \"Mundo\"", true),

        createSpec("If then expression", "fn = if True then 1 else 2", 1.toLong()),
        createSpec("If else expression", "fn = if False then 1 else 2", 2.toLong()),
        createSpec(
            "Arg access expression", """
            fn :: Char -> Char
            fn a = a
        """.trimIndent(), 'a', 'a'
        ),
        createSpec(
            "Call expression", """
            fn = sum 10 20
            sum a b = a + b
        """.trimIndent(), 30.toLong()
        ),

        createSpec("< byte expression", "fn = (byte 1) < (byte 2)", true),
        createSpec("< short expression", "fn = (short 1) < (short 2)", true),
        createSpec("< int expression", "fn = (int 1) < (int 2)", true),
        createSpec("< long expression", "fn = (long 1) < (long 2)", true),
        createSpec("< bigint expression", "fn = (bigint 1) < (bigint 2)", true),
        createSpec("< float expression", "fn = (float 1.0) < (float 2.0)", true),
        createSpec("< double expression", "fn = (double 1.0) < (double 2.0)", true),
        createSpec("< BigDecimal expression", "fn = (bigdec 1.0) < (bigdec 2.0)", true),

        createSpec("< byte expression 2", "fn = (byte 2) < (byte 1)", false),
        createSpec("< short expression 2", "fn = (short 2) < (short 1)", false),
        createSpec("< int expression 2", "fn = (int 2) < (int 1)", false),
        createSpec("< long expression 2", "fn = (long 2) < (long 1)", false),
        createSpec("< bigint expression 2", "fn = (bigint 2) < (bigint 1)", false),
        createSpec("< float expression 2", "fn = (float 2.0) < (float 1.0)", false),
        createSpec("< double expression 2", "fn = (double 2.0) < (double 1.0)", false),
        createSpec("< BigDecimal expression 2", "fn = (bigdec 2.0) < (bigdec 1.0)", false),

        createSpec("<= byte expression", "fn = (byte 1) <= (byte 2)", true),
        createSpec("<= short expression", "fn = (short 1) <= (short 2)", true),
        createSpec("<= int expression", "fn = (int 1) <= (int 2)", true),
        createSpec("<= long expression", "fn = (long 1) <= (long 2)", true),
        createSpec("<= bigint expression", "fn = (bigint 1) <= (bigint 2)", true),
        createSpec("<= float expression", "fn = (float 1.0) <= (float 2.0)", true),
        createSpec("<= double expression", "fn = (double 1.0) <= (double 2.0)", true),
        createSpec("<= BigDecimal expression", "fn = (bigdec 1.0) <= (bigdec 2.0)", true),

        createSpec("<= byte expression 2", "fn = (byte 2) <= (byte 2)", true),
        createSpec("<= short expression 2", "fn = (short 2) <= (short 2)", true),
        createSpec("<= int expression 2", "fn = (int 2) <= (int 2)", true),
        createSpec("<= long expression 2", "fn = (long 2) <= (long 2)", true),
        createSpec("<= bigint expression 2", "fn = (bigint 2) <= (bigint 2)", true),
        createSpec("<= float expression 2", "fn = (float 2.0) <= (float 2.0)", true),
        createSpec("<= double expression 2", "fn = (double 2.0) <= (double 2.0)", true),
        createSpec("<= BigDecimal expression 2", "fn = (bigdec 2.0) <= (bigdec 2.0)", true),

        createSpec("<= byte expression 3", "fn = (byte 3) <= (byte 2)", false),
        createSpec("<= short expression 3", "fn = (short 3) <= (short 2)", false),
        createSpec("<= int expression 3", "fn = (int 3) <= (int 2)", false),
        createSpec("<= long expression 3", "fn = (long 3) <= (long 2)", false),
        createSpec("<= bigint expression 3", "fn = (bigint 3) <= (bigint 2)", false),
        createSpec("<= float expression 3", "fn = (float 3.0) <= (float 2.0)", false),
        createSpec("<= double expression 3", "fn = (double 3.0) <= (double 2.0)", false),
        createSpec("<= BigDecimal expression 3", "fn = (bigdec 3.0) <= (bigdec 2.0)", false),

        createSpec(">= byte expression", "fn = (byte 1) >= (byte 2)", false),
        createSpec(">= short expression", "fn = (short 1) >= (short 2)", false),
        createSpec(">= int expression", "fn = (int 1) >= (int 2)", false),
        createSpec(">= long expression", "fn = (long 1) >= (long 2)", false),
        createSpec(">= bigint expression", "fn = (bigint 1) >= (bigint 2)", false),
        createSpec(">= float expression", "fn = (float 1.0) >= (float 2.0)", false),
        createSpec(">= double expression", "fn = (double 1.0) >= (double 2.0)", false),
        createSpec(">= BigDecimal expression", "fn = (bigdec 1.0) >= (bigdec 2.0)", false),

        createSpec(">= byte expression 2", "fn = (byte 2) >= (byte 2)", true),
        createSpec(">= short expression 2", "fn = (short 2) >= (short 2)", true),
        createSpec(">= int expression 2", "fn = (int 2) >= (int 2)", true),
        createSpec(">= long expression 2", "fn = (long 2) >= (long 2)", true),
        createSpec(">= bigint expression 2", "fn = (bigint 2) >= (bigint 2)", true),
        createSpec(">= float expression 2", "fn = (float 2.0) >= (float 2.0)", true),
        createSpec(">= double expression 2", "fn = (double 2.0) >= (double 2.0)", true),
        createSpec(">= BigDecimal expression 2", "fn = (bigdec 2.0) >= (bigdec 2.0)", true),

        createSpec(">= byte expression 3", "fn = (byte 3) >= (byte 2)", true),
        createSpec(">= short expression 3", "fn = (short 3) >= (short 2)", true),
        createSpec(">= int expression 3", "fn = (int 3) >= (int 2)", true),
        createSpec(">= long expression 3", "fn = (long 3) >= (long 2)", true),
        createSpec(">= bigint expression 3", "fn = (bigint 3) >= (bigint 2)", true),
        createSpec(">= float expression 3", "fn = (float 3.0) >= (float 2.0)", true),
        createSpec(">= double expression 3", "fn = (double 3.0) >= (double 2.0)", true),
        createSpec(">= BigDecimal expression 3", "fn = (bigdec 3.0) >= (bigdec 2.0)", true),

        createSpec("> byte expression", "fn = (byte 1) > (byte 2)", false),
        createSpec("> short expression", "fn = (short 1) > (short 2)", false),
        createSpec("> int expression", "fn = (int 1) > (int 2)", false),
        createSpec("> long expression", "fn = (long 1) > (long 2)", false),
        createSpec("> bigint expression", "fn = (bigint 1) > (bigint 2)", false),
        createSpec("> float expression", "fn = (float 1.0) > (float 2.0)", false),
        createSpec("> double expression", "fn = (double 1.0) > (double 2.0)", false),
        createSpec("> BigDecimal expression", "fn = (bigdec 1.0) > (bigdec 2.0)", false),

        createSpec("> byte expression 2", "fn = (byte 3) > (byte 2)", true),
        createSpec("> short expression 2", "fn = (short 3) > (short 2)", true),
        createSpec("> int expression 2", "fn = (int 3) > (int 2)", true),
        createSpec("> long expression 2", "fn = (long 3) > (long 2)", true),
        createSpec("> bigint expression 2", "fn = (bigint 3) > (bigint 2)", true),
        createSpec("> float expression 2", "fn = (float 3.0) > (float 2.0)", true),
        createSpec("> double expression 2", "fn = (double 3.0) > (double 2.0)", true),
        createSpec("> BigDecimal expression 2", "fn = (bigdec 3.0) > (bigdec 2.0)", true),

        createSpec("byte == expression", "fn = (byte 1) == (byte 2)", false),
        createSpec("short == expression", "fn = (short 1) == (short 2)", false),
        createSpec("int == expression", "fn = (int 1) == (int 2)", false),
        createSpec("long == expression", "fn = (long 1) == (long 2)", false),
        createSpec("bigint == expression", "fn = (bigint 1) == (bigint 2)", false),
        createSpec("float == expression", "fn = (float 1.0) == (float 2.0)", false),
        createSpec("double == expression", "fn = (double 1.0) == (double 2.0)", false),
        createSpec("BigDecimal == expression", "fn = (bigdec 1.0) == (bigdec 2.0)", false),

        createSpec("byte == expression 2", "fn = (byte 1) == (byte 1)", true),
        createSpec("short == expression 2", "fn = (short 1) == (short 1)", true),
        createSpec("int == expression 2", "fn = (int 1) == (int 1)", true),
        createSpec("long == expression 2", "fn = (long 1) == (long 1)", true),
        createSpec("bigint == expression 2", "fn = (bigint 1) == (bigint 1)", true),
        createSpec("float == expression 2", "fn = (float 1.0) == (float 1.0)", true),
        createSpec("double == expression 2", "fn = (double 1.0) == (double 1.0)", true),
        createSpec("BigDecimal == expression 2", "fn = (bigdec 1.0) == (bigdec 1.0)", true),

        createSpec("byte != expression", "fn = (byte 1) != (byte 2)", true),
        createSpec("short != expression", "fn = (short 1) != (short 2)", true),
        createSpec("int != expression", "fn = (int 1) != (int 2)", true),
        createSpec("long != expression", "fn = (long 1) != (long 2)", true),
        createSpec("bigint != expression", "fn = (bigint 1) != (bigint 2)", true),
        createSpec("float != expression", "fn = (float 1.0) != (float 2.0)", true),
        createSpec("double != expression", "fn = (double 1.0) != (double 2.0)", true),
        createSpec("BigDecimal != expression", "fn = (bigdec 1.0) != (bigdec 2.0)", true),

        createSpec("byte != expression 2", "fn = (byte 1) != (byte 1)", false),
        createSpec("short != expression 2", "fn = (short 1) != (short 1)", false),
        createSpec("int != expression 2", "fn = (int 1) != (int 1)", false),
        createSpec("long != expression 2", "fn = (long 1) != (long 1)", false),
        createSpec("bigint != expression 2", "fn = (bigint 1) != (bigint 1)", false),
        createSpec("float != expression 2", "fn = (float 1.0) != (float 1.0)", false),
        createSpec("double != expression 2", "fn = (double 1.0) != (double 1.0)", false),
        createSpec("BigDecimal != expression 2", "fn = (bigdec 1.0) != (bigdec 1.0)", false),

        createSpec("BitAnd byte expression", "fn = (byte 3) & (byte 2)", 2.toByte()),
        createSpec("BitAnd short expression", "fn = (short 3) & (short 2)", 2.toShort()),
        createSpec("BitAnd int expression", "fn = (int 3) & (int 2)", 2),
        createSpec("BitAnd long expression", "fn = (long 3) & (long 2)", 2.toLong()),
        createSpec("BitAnd bigint expression", "fn = (bigint 3) & (bigint 2)", BigInteger.valueOf(2)),

        createSpec("BitOr byte expression", "fn = (byte 1) | (byte 2)", 3.toByte()),
        createSpec("BitOr short expression", "fn = (short 1) | (short 2)", 3.toShort()),
        createSpec("BitOr int expression", "fn = (int 1) | (int 2)", 3),
        createSpec("BitOr long expression", "fn = (long 1) | (long 2)", 3.toLong()),
        createSpec("BitOr bigint expression", "fn = (bigint 1) | (bigint 2)", BigInteger.valueOf(3)),

        createSpec("BitXor byte expression", "fn = (byte 3) ^ (byte 2)", 1.toByte()),
        createSpec("BitXor short expression", "fn = (short 3) ^ (short 2)", 1.toShort()),
        createSpec("BitXor int expression", "fn = (int 3) ^ (int 2)", 1),
        createSpec("BitXor long expression", "fn = (long 3) ^ (long 2)", 1.toLong()),
        createSpec("BitXor bigint expression", "fn = (bigint 3) ^ (bigint 2)", BigInteger.valueOf(1)),

        createSpec("BitShr byte expression", "fn = (byte 16) >> (byte 3)", 2.toByte()),
        createSpec("BitShr short expression", "fn = (short 16) >> (short 3)", 2.toShort()),
        createSpec("BitShr int expression", "fn = (int 16) >> (int 3)", 2),
        createSpec("BitShr long expression", "fn = (long 16) >> (long 3)", 2.toLong()),
        createSpec("BitShr bigint expression", "fn = (bigint 16) >> (bigint 3)", BigInteger.valueOf(2)),

        createSpec("BitShl byte expression", "fn = (byte 2) << (byte 3)", 16.toByte()),
        createSpec("BitShl short expression", "fn = (short 2) << (short 3)", 16.toShort()),
        createSpec("BitShl int expression", "fn = (int 2) << (int 3)", 16),
        createSpec("BitShl long expression", "fn = (long 2) << (long 3)", 16.toLong()),
        createSpec("BitShl bigint expression", "fn = (bigint 2) << (bigint 3)", BigInteger.valueOf(16)),

        createSpec("Sum byte expression", "fn = (byte 1) + (byte 2)", 3.toByte()),
        createSpec("Sum short expression", "fn = (short 1) + (short 2)", 3.toShort()),
        createSpec("Sum int expression", "fn = (int 1) + (int 2)", 3),
        createSpec("Sum long expression", "fn = (long 1) + (long 2)", 3.toLong()),
        createSpec("Sum bigint expression", "fn = (bigint 1) + (bigint 2)", BigInteger.valueOf(3)),
        createSpec("Sum float expression", "fn = (float 1.0) + (float 2.0)", (3.0).toFloat()),
        createSpec("Sum double expression", "fn = (double 1.0) + (double 2.0)", (3.0).toDouble()),
        createSpec("Sum BigDecimal expression", "fn = (bigdec 1.0) + (bigdec 2.0)", BigDecimal.valueOf(3.0)),

        createSpec("Sub byte expression", "fn = (byte 1) - (byte 2)", (-1).toByte()),
        createSpec("Sub short expression", "fn = (short 1) - (short 2)", (-1).toShort()),
        createSpec("Sub int expression", "fn = (int 1) - (int 2)", -1),
        createSpec("Sub long expression", "fn = (long 1) - (long 2)", (-1).toLong()),
        createSpec("Sub bigint expression", "fn = (bigint 1) - (bigint 2)", BigInteger.valueOf(-1)),
        createSpec("Sub float expression", "fn = (float 1.0) - (float 2.0)", (-1.0).toFloat()),
        createSpec("Sub double expression", "fn = (double 1.0) - (double 2.0)", -1.0),
        createSpec("Sub BigDecimal expression", "fn = (bigdec 1.0) - (bigdec 2.0)", BigDecimal.valueOf(-1.0)),

        createSpec("Div byte expression", "fn = (byte 2) / (byte 2)", 1.toByte()),
        createSpec("Div short expression", "fn = (short 2) / (short 2)", 1.toShort()),
        createSpec("Div int expression", "fn = (int 2) / (int 2)", 1),
        createSpec("Div long expression", "fn = (long 2) / (long 2)", 1.toLong()),
        createSpec("Div bigint expression", "fn = (bigint 2) / (bigint 2)", BigInteger.valueOf(1)),
        createSpec("Div float expression", "fn = (float 2.0) / (float 2.0)", (1.0).toFloat()),
        createSpec("Div double expression", "fn = (double 2.0) / (double 2.0)", (1.0).toDouble()),
        createSpec(
            "Div BigDecimal expression",
            "fn = (bigdec 2.0) / (bigdec 2.0)",
            BigDecimal.valueOf(1.0)
        ),

        createSpec("Mul byte expression", "fn = (byte 2) * (byte 2)", 4.toByte()),
        createSpec("Mul short expression", "fn = (short 2) * (short 2)", 4.toShort()),
        createSpec("Mul int expression", "fn = (int 2) * (int 2)", 4),
        createSpec("Mul long expression", "fn = (long 2) * (long 2)", 4.toLong()),
        createSpec("Mul bigint expression", "fn = (bigint 2) * (bigint 2)", BigInteger.valueOf(4)),
        createSpec("Mul float expression", "fn = (float 2.0) * (float 2.0)", (4.0).toFloat()),
        createSpec("Mul double expression", "fn = (double 2.0) * (double 2.0)", (4.0).toDouble()),
        createSpec(
            "Mul BigDecimal expression",
            "fn = (bigdec 2.0) * (bigdec 2.0)",
            BigDecimal.valueOf(2.0).multiply(BigDecimal.valueOf(2.0))
        ),

        createSpec("Mod byte expression", "fn = (byte 2) % (byte 2)", 0.toByte()),
        createSpec("Mod short expression", "fn = (short 2) % (short 2)", 0.toShort()),
        createSpec("Mod int expression", "fn = (int 2) % (int 2)", 0),
        createSpec("Mod long expression", "fn = (long 2) % (long 2)", 0.toLong()),
        createSpec("Mod bigint expression", "fn = (bigint 2) % (bigint 2)", BigInteger.ZERO),
        createSpec("Mod float expression", "fn = (float 2.0) % (float 2.0)", (0.0).toFloat()),
        createSpec("Mod double expression", "fn = (double 2.0) % (double 2.0)", (0.0).toDouble()),
        createSpec(
            "Mod BigDecimal expression",
            "fn = (bigdec 2.0) % (bigdec 2.0)",
            BigDecimal.valueOf(0.0)
        ),

        createSpec("Pow byte expression", "fn = (byte 2) ** (byte 3)", 8.toByte()),
        createSpec("Pow short expression", "fn = (short 2) ** (short 3)", 8.toShort()),
        createSpec("Pow int expression", "fn = (int 2) ** (int 3)", 8),
        createSpec("Pow long expression", "fn = (long 2) ** (long 3)", 8.toLong()),
        createSpec("Pow bigint expression", "fn = (bigint 2) ** (bigint 3)", BigInteger.valueOf(8)),
        createSpec("Pow float expression", "fn = (float 2.0) ** (float 3.0)", (8.0).toFloat()),
        createSpec("Pow double expression", "fn = (double 2.0) ** (double 3.0)", (8.0).toDouble()),
        createSpec(
            "Pow BigDecimal expression",
            "fn = (bigdec 2.0) ** (bigdec 3.0)",
            BigDecimal.valueOf(2.0).pow(3)
        ),

        createSpec("Cast bigint", "fn = bigint (bigint 3)", BigInteger.valueOf(3)),
        createSpec("Cast bigint from bigdec", "fn = bigint (bigdec 3)", BigInteger.valueOf(3)),
        createSpec("Cast bigdec", "fn = bigdec (bigdec 3)", BigDecimal.valueOf(3.0)),

        createSpec(
            "Let expressions with var binding",
            """|fn = let x = 10
               |         y = 20
               |     then x + y
            """.trimMargin(),
            30.toLong()
        ),
        createSpec(
            "Let expressions with var binding - byte",
            """|fn = let x = byte 10
               |         y = byte 20
               |     then x + y
            """.trimMargin(),
            30.toByte()
        ),
        createSpec(
            "Let expressions with var binding - int",
            """|fn = let x = int 10
               |         y = int 20
               |     then x + y
            """.trimMargin(),
            30
        ),
        createSpec(
            "Let expressions with var binding - short",
            """|fn = let x = short 10
               |         y = short 20
               |     then x + y
            """.trimMargin(),
            30
        ),
        createSpec(
            "Let expressions with var binding - float",
            """|fn = let x = float 10
               |         y = float 20
               |     then x + y
            """.trimMargin(),
            30.toFloat()
        ),
        createSpec(
            "Let expressions with var binding - double",
            """|fn = let x = double 10
               |         y = double 20
               |     then x + y
            """.trimMargin(),
            30.toDouble()
        ),
        createSpec(
            "Let expressions with var binding - object",
            """|fn = let x = bigint 10
               |         y = bigint 20
               |     then x + y
            """.trimMargin(),
            BigInteger.valueOf(30)
        ),
        createSpec(
            "Let expressions with var binding - boolean",
            """|fn = let x = True
               |     then x
            """.trimMargin(),
            true
        ),
        createSpec(
            "Evaluate native method",
            """|n = fn "Hello"
               |
               |fn :: String -> Int
               |native fn a              
            """.trimMargin(),
            "Hello".length
        ),
        createSpec(
            "Evaluate calling an abstraction",
            """|n = fn 10 20
               |
               |fn :: Long -> Long -> Long
               |fn a b = a + b              
            """.trimMargin(),
            30.toLong()
        ),
        createSpec(
            "Evaluate trait abstractions",
            """|fn = double 10  
               |
               |trait Sum a =
               |    sum :: a -> a -> a
               |    double :: a -> a
               |    
               |    double a = sum a a
               |
               |impl Sum for Long =
               |    sum a b = a + b
               |            
            """.trimMargin(),
            12.toLong()
        ),
    ).forEach { (description, code, call) ->
        description {
            code.evaluateFirstFunction(call.arguments)
                .shouldBe(call.expectedResult)
        }
    }
})


class CustomEvaluationTest : StringSpec({
    "Check a custom spec" {
        createSpec(
            "Evaluate trait abstractions",
            """|fn = double 10  
               |
               |trait Sum a =
               |    sum :: a -> a -> a
               |    double :: a -> a
               |    
               |    double a = sum a a
               |
               |impl Sum for Long =
               |    sum a b = a + b
               |            
            """.trimMargin(),
            12.toLong()
        ).let { (_, code, call) ->
            code.evaluateFirstFunction(call.arguments)
                .shouldBe(call.expectedResult)
        }
    }
})
