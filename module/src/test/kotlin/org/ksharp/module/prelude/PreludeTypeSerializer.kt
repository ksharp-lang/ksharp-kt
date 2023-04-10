package org.ksharp.module.prelude

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.*
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.bufferView
import org.ksharp.common.io.newBufferWriter
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.module.prelude.types.NumericType
import org.ksharp.module.prelude.types.charType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.serializer.readTypeFrom
import org.ksharp.typesystem.serializer.readTypeSystemFrom
import org.ksharp.typesystem.serializer.writeTo
import org.ksharp.typesystem.types.Type
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

private fun mockStringTableView(items: List<String>) = object : BinaryTableView {
    override fun get(index: Int): String = items[index]
}

private inline fun <reified T : Type> T.shouldBeSerializable() {
    val stringPool = listBuilder<String>()
    val buffer = newBufferWriter()
    val table = mockStringTable(stringPool)
    val output = ByteArrayOutputStream()
    writeTo(buffer, table)
    buffer.transferTo(output)
    val stringPoolView = mockStringTableView(stringPool.build())
    val input = ByteArrayInputStream(output.toByteArray())
    input.bufferView {
        readTypeFrom<T>(it, stringPoolView).also { t -> println(t) }
    }.shouldBe(this)
}

private fun TypeSystem.shouldBeSerializable(): TypeSystem {
    val stringPool = listBuilder<String>()
    val buffer = newBufferWriter()
    val table = mockStringTable(stringPool)
    val output = ByteArrayOutputStream()
    writeTo(buffer, table)
    buffer.transferTo(output)
    val stringPoolView = mockStringTableView(stringPool.build())
    val input = ByteArrayInputStream(output.toByteArray())
    return input.bufferView {
        readTypeSystemFrom(it, stringPoolView).also { t -> println(t) }
    }
}

class PreludeTypeSerializer : StringSpec({
    "Serialize Char Type" {
        charType.shouldBeSerializable()
    }
    "Serialize Numeric Type" {
        NumericType(Numeric.BigDecimal).shouldBeSerializable()
    }
})