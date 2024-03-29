package org.ksharp.ir.serializer

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.*
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.bufferView
import org.ksharp.common.io.newBufferWriter
import org.ksharp.ir.*
import org.ksharp.module.Impl
import org.ksharp.module.prelude.preludeModule
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.types.newParameterForTesting
import org.ksharp.typesystem.types.toFunctionType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicInteger

private fun mockStringTable(items: ListBuilder<String>) = object : BinaryTable {
    private val counter = AtomicInteger(-1)
    private val dictionary = mapBuilder<String, Int>()
    override fun add(name: String): Int =
        dictionary.get(name) ?: run {
            items.add(name)
            dictionary.put(name, counter.incrementAndGet())
            counter.get()
        }
}

private fun mockStringTableView(items: List<String>) = BinaryTableView { index -> items[index] }

private inline fun <reified T : IrNode> T.shouldBeSerializable() {
    val stringPool = listBuilder<String>()
    val buffer = newBufferWriter()
    val table = mockStringTable(stringPool)
    val output = ByteArrayOutputStream()
    serialize(buffer, table)
    buffer.transferTo(output)
    val stringPoolView = mockStringTableView(stringPool.build())
    val input = ByteArrayInputStream(output.toByteArray())
    val lookup = functionLookup()
    this.shouldBe(input.bufferView {
        it.readIrNode(lookup, { _ -> null }, stringPoolView).also(::println)
    })
}

private fun IrModule.shouldBeSerializableModule() {
    val output = ByteArrayOutputStream()
    writeTo(output)
    val input = ByteArrayInputStream(output.toByteArray())
    this.shouldBe(input.bufferView {
        it.readIrModule { _ -> null }.also(::println)
    })
}

class NodeSerializerTest : StringSpec({
    val location = Location(Line(1) to Offset(1), Line(1) to Offset(5))
    val attributes = setOf(CommonAttribute.Native, CommonAttribute.Public)
    "IrInteger test" {
        IrInteger(1, location)
            .shouldBeSerializable()
    }
    "IrDecimal test" {
        IrDecimal(1.5, location)
            .shouldBeSerializable()
    }
    "IrCharacter test" {
        IrCharacter('a', location)
            .shouldBeSerializable()
    }
    "IrString test" {
        IrString("abc", location)
            .shouldBeSerializable()
    }
    "IrBool test" {
        IrBool(true, location)
            .shouldBeSerializable()
        IrBool(false, location)
            .shouldBeSerializable()
    }
    "IrNumCast test" {
        IrNumCast(IrInteger(1, location), CastType.Int, location)
            .shouldBeSerializable()
    }
    "IrToString test" {
        IrToString(IrInteger(1, location), location)
            .shouldBeSerializable()
    }
    "IrPair test" {
        IrPair(attributes, IrInteger(1, location), IrInteger(2, location), location)
            .shouldBeSerializable()
    }
    "IrList test" {
        IrList(attributes, listOf(IrInteger(1, location), IrInteger(2, location)), location)
            .shouldBeSerializable()
    }
    "IrSet test" {
        IrSet(attributes, listOf(IrInteger(1, location), IrInteger(2, location)), location)
            .shouldBeSerializable()
    }
    "IrMap test" {
        IrMap(
            attributes,
            listOf(
                IrPair(attributes, IrInteger(1, location), IrInteger(2, location), location),
                IrPair(attributes, IrInteger(3, location), IrInteger(4, location), location)
            ),
            location
        )
            .shouldBeSerializable()
    }
    "IrSum test" {
        IrSum(attributes, IrInteger(1, location), IrInteger(2, location), location)
            .shouldBeSerializable()
    }
    "IrSub test" {
        IrSub(attributes, IrInteger(1, location), IrInteger(2, location), location)
            .shouldBeSerializable()
    }
    "IrMul test" {
        IrMul(attributes, IrInteger(1, location), IrInteger(2, location), location)
            .shouldBeSerializable()
    }
    "IrDiv test" {
        IrDiv(attributes, IrInteger(1, location), IrInteger(2, location), location)
            .shouldBeSerializable()
    }
    "IrMod test" {
        IrMod(attributes, IrInteger(1, location), IrInteger(2, location), location)
            .shouldBeSerializable()
    }
    "IrPow test" {
        IrPow(attributes, IrInteger(1, location), IrInteger(2, location), location)
            .shouldBeSerializable()
    }
    "IrArithmeticCall Test" {
        IrArithmeticCall("+", IrSum(attributes, IrInteger(1, location), IrInteger(2, location), location))
            .shouldBeSerializable()
    }
    "IrArg test" {
        IrArg(attributes, NoCaptured, 1, location)
            .shouldBeSerializable()
    }
    "IrVar test" {
        IrVar(attributes, NoCaptured, 1, location)
            .shouldBeSerializable()
    }
    "IrArg captured test" {
        IrArg(attributes, "Test", 1, location)
            .shouldBeSerializable()
    }
    "IrVar captured test" {
        IrVar(attributes, "Test", 1, location)
            .shouldBeSerializable()
    }
    "IrIf test" {
        IrIf(
            attributes,
            IrBool(true, location),
            IrInteger(1, location),
            IrInteger(2, location),
            location
        )
            .shouldBeSerializable()
    }
    "IrFunction test" {
        IrFunction(
            attributes,
            "test",
            listOf("a", "b"),
            1,
            IrInteger(1, location),
            location
        )
            .shouldBeSerializable()
    }
    "IrModule test" {
        IrModule(
            listOf(
                IrFunction(
                    attributes,
                    "test",
                    listOf("a", "b"),
                    1,
                    IrInteger(1, location),
                    location
                )
            ),
            mapOf(
                "Trait" to listOf(
                    IrFunction(
                        attributes,
                        "test",
                        listOf("a", "b"),
                        1,
                        IrInteger(1, location),
                        location
                    )
                )
            ),
            mapOf(
                Impl("", "Trait", newParameterForTesting(1)) to listOf(
                    IrFunction(
                        attributes,
                        "test",
                        listOf("a", "b"),
                        1,
                        IrInteger(1, location),
                        location
                    )
                )
            )
        ).apply {
            shouldBeSerializable()
            shouldBeSerializableModule()
        }

    }
    "IrCall test" {
        IrCall(
            attributes,
            "test",
            CallScope("test", null, null),
            listOf(IrInteger(1, location), IrInteger(2, location)),
            newParameterForTesting(2),
            location
        )
            .shouldBeSerializable()
    }
    "IrCall test 2" {
        IrCall(
            attributes,
            "test",
            CallScope("test", "T", "T"),
            listOf(IrInteger(1, location), IrInteger(2, location)),
            newParameterForTesting(2),
            location
        )
            .shouldBeSerializable()
    }
    "IrLet test" {
        IrLet(
            attributes,
            listOf(
                IrInteger(1, location),
                IrInteger(2, location),
            ),
            location
        )
            .shouldBeSerializable()
    }
    "IrLetSetVar test" {
        IrSetVar(
            attributes,
            0,
            IrInteger(2, location),
            location
        )
            .shouldBeSerializable()
    }
    "NativeCall Test" {
        IrNativeCall(
            attributes,
            "test",
            listOf(IrInteger(1, location), IrInteger(2, location)),
            newParameterForTesting(2),
            location
        )
            .shouldBeSerializable()
    }
    "ModuleCall Test" {
        IrModuleCall(
            attributes,
            { _ -> null },
            "test",
            "name",
            listOf(IrInteger(1, location), IrInteger(2, location)),
            listOf(newParameterForTesting(2)).toFunctionType(preludeModule.typeSystem, emptySet()),
            location
        ).shouldBeSerializable()
    }
    "ModuleCall Test 2" {
        IrModuleCall(
            attributes,
            "test",
            "name",
            listOf(IrInteger(1, location), IrInteger(2, location)),
            listOf(newParameterForTesting(2)).toFunctionType(preludeModule.typeSystem, emptySet()),
            location
        ).shouldBeSerializable()
    }
    "Comparable Test" {
        IrComparable(
            IrModuleCall(
                attributes,
                "test",
                "name",
                listOf(IrInteger(1, location), IrInteger(2, location)),
                listOf(newParameterForTesting(2)).toFunctionType(preludeModule.typeSystem, emptySet()),
                location
            ),
            listOf(Less, Equal)
        ).shouldBeSerializable()
    }
    "IrLambda Test" {
        IrLambda(
            attributes,
            emptyList(),
            listOf("a"),
            4,
            IrInteger(1, location),
            location
        ).shouldBeSerializable()
    }
    "IrLambdaCall Test" {
        IrLambdaCall(
            attributes,
            IrInteger(1, location),
            listOf(IrInteger(1, location)),
            newParameterForTesting(0),
            location
        ).shouldBeSerializable()
    }
    "CaptureVar Test" {
        IrCaptureVar(
            "a",
            IrVar(
                attributes,
                NoCaptured,
                1,
                location
            )
        ).shouldBeSerializable()
    }
})
