package org.ksharp.compiler.transpiler

class TranspilerBuffer(
    private val indent: String = "",
    private val startIndent: String = indent
) {
    private var currentIndent: String? = null
    private var start: Boolean = false
    private val buffer = StringBuilder()

    private fun calculateIndent(indent: String?): String {
        if (indent == null) {
            currentIndent = if (currentIndent == null) {
                startIndent
            } else this.indent
            return currentIndent!!
        }
        return indent
    }

    fun beginLn(indent: String? = null): TranspilerBuffer {
        if (!start) {
            buffer.append(calculateIndent(indent))
            start = true
        }
        return this
    }

    fun endLn(): TranspilerBuffer {
        buffer.append(NEWLINE)
        start = false
        return this
    }

    fun append(content: String, indent: String? = null): TranspilerBuffer {
        beginLn(indent)
        buffer.append(content)
        return this
    }

    fun appendLn(content: String, indent: String? = null): TranspilerBuffer {
        append(content, indent)
        endLn()
        return this
    }

    fun merge(buffer: TranspilerBuffer): TranspilerBuffer {
        this.buffer.append(buffer.toString())
        return this
    }

    operator fun String.unaryPlus() = appendLn(this)

    fun indent(indent: String, init: TranspilerBuffer.() -> Unit): TranspilerBuffer {
        val subBuffer = TranspilerBuffer("${this.indent}$indent")
        subBuffer.init()
        buffer.append(subBuffer.toString())
        return this
    }

    fun new(init: TranspilerBuffer.() -> Unit): TranspilerBuffer {
        val buffer = TranspilerBuffer(this.indent)
        buffer.init()
        return buffer
    }

    override fun toString() = buffer.toString()

    companion object {
        const val NEWLINE = "\n"
    }
}
