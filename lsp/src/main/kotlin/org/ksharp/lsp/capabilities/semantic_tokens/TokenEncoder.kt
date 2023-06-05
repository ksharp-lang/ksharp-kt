package org.ksharp.lsp.capabilities.semantic_tokens

import org.ksharp.common.*
import org.ksharp.lsp.client.ClientLogger
import kotlin.math.pow


class TokenEncoderSpecTokens(
    private val tokensDict: MapBuilder<String, Int>,
    private val tokens: ListBuilder<String>
) {
    operator fun String.unaryPlus() {
        val id = tokens.size()
        tokensDict.put(this, id)
        tokens.add(this)
    }
}

class TokenEncoderSpecModifiers(
    private val modifiersDict: MapBuilder<String, Int>,
    private val modifiers: ListBuilder<String>
) {
    operator fun String.unaryPlus() {
        val id = 2.0.pow(modifiers.size().toDouble()).toInt()
        modifiersDict.put(this, id)
        modifiers.add(this)
    }
}

class TokenEncoderSpecBuilder {
    private var tokensDict = mapBuilder<String, Int>()
    private var tokens = listBuilder<String>()
    private var modifiersDict = mapBuilder<String, Int>()
    private var modifiers = listBuilder<String>()

    fun tokens(builder: TokenEncoderSpecTokens.() -> Unit) {
        TokenEncoderSpecTokens(tokensDict, tokens).apply(builder)
    }

    fun modifiers(builder: TokenEncoderSpecModifiers.() -> Unit) {
        TokenEncoderSpecModifiers(modifiersDict, modifiers).apply(builder)
    }

    fun build() = TokenEncoderSpec(tokens.build(), modifiers.build(), tokensDict.build(), modifiersDict.build())
}

data class TokenEncoderSpec(
    val tokens: List<String>,
    val modifiers: List<String>,
    private val tokensDict: Map<String, Int>,
    private val modifiersDict: Map<String, Int>
) {
    fun encoder() = TokenEncoder(tokensDict, modifiersDict)
}

class TokenEncoder(
    private val tokens: Map<String, Int>,
    private val modifiers: Map<String, Int>
) {

    private val encoderData = listBuilder<Int>()
    private var currentLine = 0
    private var currentOffset = 0

    fun register(line: Int, startOffset: Int, length: Int, token: String, vararg modifiers: String) {
        tokens[token]?.let { t ->
            val offsetLine = if (line != currentLine) {
                val resultLine = line - currentLine
                val isFirstTime = currentLine == 0
                currentLine = line
                currentOffset = 0
                if (isFirstTime) resultLine - 1
                else resultLine
            } else 0
            val offset = startOffset - currentOffset
            currentOffset = startOffset
            encoderData.add(offsetLine)
            encoderData.add(offset)
            encoderData.add(length)
            encoderData.add(t)
            encoderData.add(modifiers.asSequence().map {
                this.modifiers[it] ?: 0
            }.sum())

            ClientLogger.info("data: [$offsetLine $offset $length $t 0]")
        }
    }

    fun data(): List<Int> = encoderData.build()

}

fun tokenEncoderSpec(builder: TokenEncoderSpecBuilder.() -> Unit) =
    TokenEncoderSpecBuilder().apply(builder).build()
