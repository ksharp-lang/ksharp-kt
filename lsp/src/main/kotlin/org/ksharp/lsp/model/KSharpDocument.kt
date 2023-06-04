package org.ksharp.lsp.model


typealias Position = Pair<Int, Int>

data class Range(
    val start: Position,
    val end: Position,
    val length: Int
)

private fun Int.createOffset(newLineLength: Int) =
    if (this != -1)
        this to newLineLength
    else null

private fun CharSequence.calculateLines(offset: Int): Sequence<Int> =
    generateSequence(0 to 0) { (offset, newlineLength) ->
        if (newlineLength == -1) null
        else (offset + newlineLength).let { startIndex ->
            this.indexOf("\r\n", startIndex).createOffset(2)
                ?: this.indexOf("\n", startIndex).createOffset(1)
                ?: this.indexOf("\r", startIndex).createOffset(1)
                ?: (this.length to -1)
        }
    }.drop(1)
        .map { it.first + offset }

class KSharpDocument(private val data: StringBuilder) {

    private var linePositions = emptyList<Int>()

    init {
        linePositions = listOf(0) + data.calculateLines(0)
    }

    private fun offset(position: Position): Int {
        val (line, offset) = position
        return linePositions[line] + offset + (if (line != 0) 1 else 0)
    }

    private fun recalculateOffsets(range: Range, startOffset: Int, content: String) {
        val (start, end, length) = range
        val startLine = start.first
        val endLine = end.first
        val endOffset = startOffset + length
        val contentLength = content.length

        val topLines = linePositions.subList(0, startLine + 1)
        val endLines = linePositions.subList(endLine, linePositions.size)
            .filter { it >= endOffset }
            .map { it - length + contentLength }

        val endRangeStartOffset = linePositions[endLine]
        val endRangeEndOffset = linePositions[endLine + 1] - 1
        val middleOffset = (endRangeEndOffset - endRangeStartOffset - length).coerceAtLeast(0)

        val newLines = content.calculateLines(middleOffset + topLines.last() + 1)
            .toList()
            .dropLast(1)

        val result = topLines + newLines + endLines
        linePositions = listOf(0) + data.calculateLines(0)
        println("$result -- $linePositions -- $topLines -- $endLines -- $newLines")
    }

    val length: Int get() = data.length

    val lines: Int get() = linePositions.size - 1

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

    fun update(range: Range, content: String) {
        val start = offset(range.start)
        val end = start + range.length
        data.replace(start, end, content)
        recalculateOffsets(range, start, content)
    }

    override fun toString(): String = content

    fun asSequence(): Sequence<String> =
        (0 until lines).asSequence().map {
            line(it)
        }
}

fun document(content: String) = KSharpDocument(StringBuilder(content))
