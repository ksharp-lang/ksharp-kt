package org.ksharp.lsp.capabilities.actions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.lsp.capabilities.semantic_tokens.tokenEncoderSpec

class TokenEncoderTest : StringSpec({
    "Token encoder spec test" {
        val encoder = tokenEncoderSpec {
            tokens {
                +"a"
                +"b"
            }
            modifiers {
                +"c"
            }
        }
        encoder.tokens.shouldBe(listOf("a", "b"))
        encoder.modifiers.shouldBe(listOf("c"))
    }
    "Token encoder test" {
        val spec = tokenEncoderSpec {
            tokens {
                +"a"
                +"b"
            }
            modifiers {
                +"c"
                +"d"
            }
        }
        spec.encoder().apply {
            this.register(1, 0, 1, "a", "c")
            this.register(1, 7, 5, "a", "c", "d")
            this.register(1, 15, 2, "b", "d")
            this.register(3, 0, 1, "a", "c")
        }.data().shouldBe(listOf(0, 0, 1, 0, 1, 0, 7, 5, 0, 3, 0, 8, 2, 1, 2, 2, 0, 1, 0, 1))
    }
})
