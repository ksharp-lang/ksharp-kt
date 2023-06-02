package org.ksharp.lsp.model


typealias Position = Pair<Int, Int>

data class Range(
    val start: Position,
    val end: Position,
    val length: Int
)

class KSharpDocument(private val data: StringBuilder) {

    private var linePositions = emptyList<Int>()

    init {
        linePositions = listOf(0) + data.asSequence().calculateLines(0)
    }

    private fun Sequence<Char>.calculateLines(offset: Int) =
        mapIndexed { index, c ->
            if (c == '\n') index + offset
            else -1
        }.filter { it != -1 }
            .toList()

    val length: Int get() = data.length

    val lines: Int get() = linePositions.size

    val content: String get() = data.toString()

    fun line(index: Int): String {
        val start = linePositions[index].let {
            if (index == 0) it
            else it + 1
        }
        val endLine = index + 1
        return if (lines == endLine) {
            data.substring(start)
        } else data.substring(start, linePositions[endLine])
    }

    private fun offset(position: Position): Int {
        val (line, offset) = position
        return linePositions[line] + offset + (if (line != 0 && offset == 0) 1 else 0)
    }

    fun update(range: Range, content: String) {
        val start = offset(range.start)
        val end = start + range.length
        data.replace(start, end, content)
        val startLine = range.start.first
        val topLines = linePositions.subList(0, startLine + 1)
        val startOffset = topLines.last()
        linePositions = topLines +
                data.substring(startOffset + 1)
                    .asSequence()
                    .calculateLines(startOffset + 1).also { println(it) }
    }

    override fun toString(): String = content

}

fun document(content: String) = KSharpDocument(StringBuilder(content))
