package org.ksharp.module.bytecode

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.io.bufferView
import org.ksharp.common.io.newBufferWriter
import org.ksharp.common.listBuilder
import org.ksharp.module.FunctionInfo
import org.ksharp.typesystem.types.newParameter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private fun FunctionInfo.shouldBeSerializable() {
    val stringPool = listBuilder<String>()
    val buffer = newBufferWriter()
    val table = mockStringTable(stringPool)
    val output = ByteArrayOutputStream()
    writeTo(buffer, table)
    buffer.transferTo(output)
    val stringPoolView = mockStringTableView(stringPool.build())
    val input = ByteArrayInputStream(output.toByteArray())
    input.bufferView {
        it.readFunctionInfo(stringPoolView)
    }.shouldBe(this)
}

private fun Map<String, List<FunctionInfo>>.shouldBeSerializable() {
    val stringPool = listBuilder<String>()
    val buffer = newBufferWriter()
    val table = mockStringTable(stringPool)
    val output = ByteArrayOutputStream()
    writeTo(buffer, table)
    buffer.transferTo(output)
    val stringPoolView = mockStringTableView(stringPool.build())
    val input = ByteArrayInputStream(output.toByteArray())
    input.bufferView {
        it.readFunctionInfoTable(stringPoolView)
    }.shouldBe(this)
}

class FunctionSerializerTest : StringSpec({
    "Serialize FunctionInfo with dependency = null" {
        FunctionInfo(
            null,
            "sum",
            listOf(newParameter(), newParameter())
        ).shouldBeSerializable()
    }
    "Serialize FunctionInfo" {
        FunctionInfo(
            "math",
            "sum",
            listOf(newParameter(), newParameter())
        ).shouldBeSerializable()
    }
    "Serialize FunctionInfoTable" {
        mapOf(
            "sum" to listOf(
                FunctionInfo(
                    null,
                    "sum",
                    listOf(newParameter(), newParameter())
                )
            ),
            "sub" to listOf(
                FunctionInfo(
                    null,
                    "sub",
                    listOf(newParameter(), newParameter())
                )
            )
        ).shouldBeSerializable()
    }
})