package org.ksharp.parser

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class CharStreamTest : StringSpec({
    "Given a CharStream should read a char, get tokens" {
        "Hello".charStream().let {
            it.read().shouldBe('H')
            it.read().shouldBe('e')
            it.read().shouldBe('l')
            it.read().shouldBe('l')
            it.read().shouldBe('o')
            it.read().shouldBeNull()
            it.token(4).shouldBe(TextToken("He", 0, 2))
            it.read().shouldBe('l')
            it.token(0).shouldBe(TextToken("l", 2, 3))
            it.read().shouldBe('l')
            it.read().shouldBe('o')
            it.read().shouldBeNull()
            it.token(2).shouldBe(TextToken("l", 3, 4))
            it.read().shouldBe('o')
            it.read().shouldBeNull()
            it.token(0).shouldBe(TextToken("o", 4, 5))
            it.read().shouldBeNull()
            it.token(0).shouldBeNull()
        }
    }
})
