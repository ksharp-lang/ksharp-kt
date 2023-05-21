package org.ksharp.compiler.transpiler


import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TranspilerBufferTest : StringSpec({
    "Given a buffer call beginLn and expected result an indentation" {
        val buffer = TranspilerBuffer(indent = "\t")
        buffer.beginLn()
            .toString()
            .shouldBe("\t")
    }


    "Given a buffer call append to add content an a indentation" {
        val buffer = TranspilerBuffer(indent = "\t")
        val result = buffer.append("Hello").toString()
        result.shouldBe("\tHello")
    }


    "Given a buffer call append and the beginLn the last beginLn wont affect the output" {
        val buffer = TranspilerBuffer(indent = "\t")
        val result = buffer.append("Hello").beginLn().toString()
        result.shouldBe("\tHello")
    }


    "Given a buffer call endLn does beginLn and add new line" {
        val buffer = TranspilerBuffer(indent = "\t")
        val result = buffer.endLn().toString()
        result.shouldBe("\n")
    }


    "Given a buffer call appendLn does beginLn, append Text, and add new line" {
        val buffer = TranspilerBuffer(indent = "\t")
        val result = buffer.appendLn("Hello").toString()
        result.shouldBe("\tHello\n")
    }


    "Given a buffer call indent and then appendLn add double indent and append content" {
        val buffer = TranspilerBuffer(indent = "\t")
        val result = buffer.indent("\t") {
            appendLn("Hello")
        }.toString()
        result.shouldBe("\t\tHello\n")
    }

    "Given a buffer add content an then create a new buffer and add content the expected result is only the new content" {
        val buffer = TranspilerBuffer(indent = "\t")
        val result = buffer.append("Hello")
            .new {
                append("World")
            }.toString()
        result.shouldBe("\tWorld")
        buffer.toString().shouldBe("\tHello")
    }

    "Given a buffer add content an then create a new buffer and add content then merge the later buffer" {
        val buffer = TranspilerBuffer(indent = "\t")
        buffer.append("Hello")
        buffer.merge(buffer.new {
            append("World")
        })
        val result = buffer.toString()
        result.shouldBe("\tHello\tWorld")
    }

    "Given a buffer add content with custom indent" {
        val buffer = TranspilerBuffer(indent = "\t")
        buffer.append("Hello", "")
        val result = buffer.toString()
        result.shouldBe("Hello")
    }
})