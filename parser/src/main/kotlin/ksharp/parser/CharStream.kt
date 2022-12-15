package ksharp.parser

import org.ksharp.common.Position
import java.io.Closeable
import java.io.Reader
import java.io.StringReader

data class TextToken(
    val text: String,
    val start: Position,
    val end: Position
)

class CharStream internal constructor(
    private val reader: Reader
) : Closeable by reader {
    private val cache = StringBuilder()
    private var positions = arrayOf<Position>()

    private var lastPosition = 1 to 0

    private var offset = 0
    private var pendingOffset = 0
    private var consumed = false

    fun read(): Char? =
        (if (pendingOffset == 0) {
            val nc = reader.read().takeIf { it != -1 }?.toChar()
            if (nc != null) {
                cache.append(nc)
            } else {
                consumed = true
            }
            nc
        } else {
            val c = cache[cache.length - pendingOffset]
            pendingOffset -= 1
            c
        }).also {

        }


    fun token(skip: Int): TextToken? {
        if (skip > MaxSkipAllowed) throw Exception("Invalid skip value $skip. It should be until $MaxSkipAllowed")
        if (cache.isEmpty()) return null
        val cSkip = (if (consumed && pendingOffset == 0) {
            skip - 1
        } else skip).coerceAtLeast(0)
        val len = cache.length - pendingOffset - cSkip
        val text = cache.substring(0, len)
        val result = TextToken(text, positions[0], positions[positions.size - skip])
        positions = mutableListOf(*positions.subList(positions.size - skip, positions.size).toTypedArray())
        lastPosition = positions.last()
        offset += len
        pendingOffset += cSkip
        cache.delete(0, len)
        return result
    }

    companion object {
        const val MaxSkipAllowed = 10
    }
}

fun Reader.charStream(): CharStream = CharStream(this)

fun String.charStream(): CharStream = StringReader(this).charStream()