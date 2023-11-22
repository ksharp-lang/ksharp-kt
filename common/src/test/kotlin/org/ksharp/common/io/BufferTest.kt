package org.ksharp.common.io

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class BufferTest : StringSpec({
    "Test buffer writer and buffer view" {
        val output = ByteArrayOutputStream()
        newBufferWriter().apply {
            add(1)
            add(5)
            add("Hello")
            add(6)
            add(150L)
            add(15.0)
            set(0, 78)
            size.shouldBe(33)
            transferTo(output)
        }
        val bytes = output.toByteArray()
        bytes.size.shouldBe(33)
        ByteArrayInputStream(bytes).bufferView {
            it.offset.shouldBe(0)
            it.readInt(0).shouldBe(78)
            it.readInt(4).shouldBe(5)
            it.readString(8, 5).shouldBe("Hello")
            it.readInt(13).shouldBe(6)
            it.readLong(17).shouldBe(150L)
            it.readDouble(25).shouldBe(15.0)
        }.shouldNotBeNull()
    }
    "Test buffer view with offset" {
        val output = ByteArrayOutputStream()
        newBufferWriter().apply {
            add(1)
            add(5)
            add("Hello")
            add(6)
            set(0, 78)
            size.shouldBe(17)
            transferTo(output)
        }
        val bytes = output.toByteArray()
        bytes.size.shouldBe(17)
        ByteArrayInputStream(bytes).bufferView {
            it.bufferFrom(8)
                .apply {
                    offset.shouldBe(8)
                    readString(0, 5).shouldBe("Hello")
                }
            it.bufferFrom(4)
                .bufferFrom(4)
                .apply {
                    offset.shouldBe(8)
                    readString(0, 5).shouldBe("Hello")
                }
        }.shouldNotBeNull()
    }
    "Transfer a buffer to another buffer" {
        val output = ByteArrayOutputStream()
        newBufferWriter().apply {
            add(1)
            add(5)
            newBufferWriter().apply {
                add("Hello")
                add(6)
            }.transferTo(this)
            set(0, 78)
            size.shouldBe(17)
            transferTo(output)
        }
        val bytes = output.toByteArray()
        bytes.size.shouldBe(17)
        ByteArrayInputStream(bytes).bufferView {
            it.offset.shouldBe(0)
            it.readInt(0).shouldBe(78)
            it.readInt(4).shouldBe(5)
            it.readString(8, 5).shouldBe("Hello")
            it.readInt(13).shouldBe(6)
        }.shouldNotBeNull()
    }
})
