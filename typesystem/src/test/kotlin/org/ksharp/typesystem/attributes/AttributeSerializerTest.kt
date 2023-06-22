package org.ksharp.typesystem.attributes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.*
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.bufferView
import org.ksharp.common.io.newBufferWriter
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
