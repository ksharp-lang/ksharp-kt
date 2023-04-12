package org.ksharp.module.bytecode

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.io.bufferView
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class StringPoolTest : StringSpec({
    "Create StringPoolBuilder and StringPoolView" {
        val output = ByteArrayOutputStream()
        StringPoolBuilder().apply {
            add("Hello").shouldBe(0)
            add("World").shouldBe(1)
            add("Hello").shouldBe(0)
            size.shouldBe(30)
            writeTo(output)
        }
        val bytes = output.toByteArray()
        ByteArrayInputStream(bytes).bufferView { view ->
            StringPoolView(view).apply {
                size.shouldBe(2)
                this[0].shouldBe("Hello")
                this[1].shouldBe("World")
            }
        }.shouldNotBeNull()
    }
})