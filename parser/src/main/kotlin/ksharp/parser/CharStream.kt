package ksharp.parser

import java.io.Closeable
import java.io.Reader
import java.io.StringReader

data class TextToken(
    val text: String,
    val startOffset: Int,
    val endOffset: Int
)

class CharStream internal constructor(
    private val reader: Reader
) : Closeable by reader {
    private val cache = StringBuilder()
    private var offset = 0
    private var pendingOffset = 0
    private var consumed = false

    fun read(): Char? =
        if (pendingOffset == 0) {
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
        }


    fun token(skip: Int): TextToken? {
        if (cache.isEmpty()) return null
        val cSkip = (if (consumed && pendingOffset == 0) {
            skip - 1
        } else skip).coerceAtLeast(0)
        val len = cache.length - pendingOffset - cSkip
        val text = cache.substring(0, len)
        val result = TextToken(text, offset, offset + len - 1)
        offset += len
        pendingOffset += cSkip
        cache.delete(0, len)
        return result
    }
}

fun Reader.charStream(): CharStream = CharStream(this)

fun String.charStream(): CharStream = StringReader(this).charStream()