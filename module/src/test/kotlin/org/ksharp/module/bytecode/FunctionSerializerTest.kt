package org.ksharp.module.bytecode

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.MockHandlePromise
import org.ksharp.common.cast
import org.ksharp.common.io.bufferView
import org.ksharp.common.io.newBufferWriter
import org.ksharp.common.listBuilder
import org.ksharp.module.FunctionInfo
import org.ksharp.module.FunctionInfoImpl
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NameAttribute
import org.ksharp.typesystem.attributes.nameAttribute
import org.ksharp.typesystem.typeSystem
import org.ksharp.typesystem.types.newParameter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private fun FunctionInfo.shouldBeSerializable(): FunctionInfo {
    val stringPool = listBuilder<String>()
    val buffer = newBufferWriter()
    val table = mockStringTable(stringPool)
    val output = ByteArrayOutputStream()
    writeTo(buffer, table)
    buffer.transferTo(output)
    val stringPoolView = mockStringTableView(stringPool.build())
    val input = ByteArrayInputStream(output.toByteArray())
    return input.bufferView {
        it.readFunctionInfo(MockHandlePromise(), stringPoolView)
    }.apply {
        shouldBe(this)
    }
}

private fun Map<String, FunctionInfo>.shouldBeSerializable() {
    val stringPool = listBuilder<String>()
    val buffer = newBufferWriter()
    val table = mockStringTable(stringPool)
    val output = ByteArrayOutputStream()
    writeTo(buffer, table)
    buffer.transferTo(output)
    val stringPoolView = mockStringTableView(stringPool.build())
    val input = ByteArrayInputStream(output.toByteArray())
    this.shouldBe(input.bufferView {
        it.readFunctionInfoTable(MockHandlePromise(), stringPoolView)
    })
}

class FunctionSerializerTest : StringSpec({
    val ts = typeSystem { }.value
    "Serialize FunctionInfo with dependency = null" {
        FunctionInfoImpl(
            setOf(CommonAttribute.Native, CommonAttribute.Public),
            "sum",
            listOf(ts.newParameter())
        ).shouldBeSerializable()
    }
    "Serialize FunctionInfo" {
        FunctionInfoImpl(
            setOf(CommonAttribute.Internal),
            "sum",
            listOf(ts.newParameter(), ts.newParameter())
        ).shouldBeSerializable()
    }
    "Serialize FunctionInfo with annotations" {
        FunctionInfoImpl(
            setOf(
                CommonAttribute.Native,
                CommonAttribute.Internal,
                nameAttribute(mapOf("java" to "sum", "c#" to "Sum"))
            ),
            "sum",
            listOf(ts.newParameter(), ts.newParameter())
        ).shouldBeSerializable()
            .attributes
            .first { it is NameAttribute }
            .cast<NameAttribute>()
            .value
            .shouldBe(mapOf("java" to "sum", "c#" to "Sum"))
    }
    "Serialize FunctionInfo complete" {
        FunctionInfoImpl(
            setOf(CommonAttribute.Internal),
            "sum",
            listOf(ts.newParameter(), ts.newParameter())
        ).shouldBeSerializable()
    }
    "Serialize FunctionInfoTable" {
        mapOf(
            "sum" to FunctionInfoImpl(
                setOf(CommonAttribute.Native, CommonAttribute.Public),
                "sum",
                listOf(ts.newParameter(), ts.newParameter())
            ),
            "sub" to FunctionInfoImpl(
                setOf(CommonAttribute.Public),
                "sub",
                listOf(ts.newParameter(), ts.newParameter())
            )
        ).shouldBeSerializable()
    }
})
