package org.ksharp.module.bytecode

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.cast
import org.ksharp.common.io.bufferView
import org.ksharp.common.io.newBufferWriter
import org.ksharp.common.listBuilder
import org.ksharp.module.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private fun Attribute.shouldBeSerializable(): Attribute {
    val stringPool = listBuilder<String>()
    val buffer = newBufferWriter()
    val table = mockStringTable(stringPool)
    val output = ByteArrayOutputStream()
    writeTo(buffer, table)
    buffer.transferTo(output)
    val stringPoolView = mockStringTableView(stringPool.build())
    val input = ByteArrayInputStream(output.toByteArray())
    return input.bufferView {
        it.readAttribute(stringPoolView).also { println(it) }
    }.apply {
        shouldBe(this)
    }
}

private fun Set<Attribute>.shouldBeSerializable() {
    val stringPool = listBuilder<String>()
    val buffer = newBufferWriter()
    val table = mockStringTable(stringPool)
    val output = ByteArrayOutputStream()
    writeTo(buffer, table)
    buffer.transferTo(output)
    val stringPoolView = mockStringTableView(stringPool.build())
    val input = ByteArrayInputStream(output.toByteArray())
    input.bufferView {
        it.readAttributes(stringPoolView).also { println(it) }
    }.shouldBe(this)
}

class AttributeSerializerTest : StringSpec({
    "Attribute Serializer" {
        CommonAttribute.Native.shouldBeSerializable()
    }
    "Name attribute serializer" {
        nameAttribute(mapOf("java" to "name", "c#" to "name2"))
            .shouldBeSerializable()
            .cast<NameAttribute>()
            .value.shouldBe(mapOf("java" to "name", "c#" to "name2"))
    }
    "Set of attributes" {
        setOf(CommonAttribute.Public, nameAttribute(mapOf("java" to "name")))
            .shouldBeSerializable()
    }
})
