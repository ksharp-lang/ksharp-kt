package org.ksharp.typesystem.annotations

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

private fun Annotation.shouldBeSerializable(): Annotation {
    val stringPool = listBuilder<String>()
    val buffer = newBufferWriter()
    val table = mockStringTable(stringPool)
    val output = ByteArrayOutputStream()
    writeTo(buffer, table)
    buffer.transferTo(output)
    val stringPoolView = mockStringTableView(stringPool.build())
    val input = ByteArrayInputStream(output.toByteArray())
    return input.bufferView {
        it.readAnnotation(stringPoolView)
            .also { println(it) }
    }.also { it.shouldBe(this) }
}

private fun List<Annotation>.shouldBeSerializable(): List<Annotation> {
    val stringPool = listBuilder<String>()
    val buffer = newBufferWriter()
    val table = mockStringTable(stringPool)
    val output = ByteArrayOutputStream()
    writeTo(buffer, table)
    buffer.transferTo(output)
    val stringPoolView = mockStringTableView(stringPool.build())
    val input = ByteArrayInputStream(output.toByteArray())
    return input.bufferView {
        it.readAnnotations(stringPoolView)
    }.also { it.shouldBe(this) }
}

class AnnotationSerializerTest : StringSpec({
    "Serialize annotation without attributes" {
        Annotation("simple", mapOf()).shouldBeSerializable()
    }
    "Serialize annotation with attributes" {
        annotation("Test") {
            set("key1", "test")
            set("key2", true)
            set("key3", false)
            set("key4", Annotation("TestChild", mapOf("key1" to "value1")))
        }.shouldBeSerializable()
    }
    "Serialize annotation with list" {
        annotation("Test") {
            set("key1", listOf("value 1", "value 2", "value 3"))
        }.shouldBeSerializable()
    }
    "Serialize list of annotation" {
        listOf(
            annotation("Test") {
                set("key1", "test")
            },
            annotation("Test2") {
                set("key1", "test")
            }
        ).shouldBeSerializable()
    }
})
