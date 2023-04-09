package org.ksharp.module.bytecode

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.netty.buffer.Unpooled
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class StringPoolTest : StringSpec({
    "Create StringPoolBuilder and StringPoolView" {
        val output = ByteArrayOutputStream()
        StringPoolBuilder().apply {
            add("Hello")
            add("World")
            writeTo(output)
        }
        val bytes = output.toByteArray()
        val buffer = Unpooled.buffer().apply { writeBytes(ByteArrayInputStream(bytes), bytes.size) }
        StringPoolView(0, BufferView(buffer)).apply {
            this[0].shouldBe("Hello")
            this[1].shouldBe("World")
        }
    }
})