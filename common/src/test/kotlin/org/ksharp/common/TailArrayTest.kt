package org.ksharp.common

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TailArrayTest : StringSpec({
    "Check empty array" {
        tailArray<String>(5)
            .apply {
                size.shouldBe(0)
                shouldThrow<IndexOutOfBoundsException> {
                    get(0)
                }.shouldBe(IndexOutOfBoundsException("0 >= 0"))
            }
    }
    "Size less than capacity" {
        tailArray<String>(5).apply {
            append("H")
            append("E")
            append("L")
            size.shouldBe(3)
            get(0).shouldBe("H")
            get(1).shouldBe("E")
            get(2).shouldBe("L")
            shouldThrow<IndexOutOfBoundsException> {
                get(4)
            }.shouldBe(IndexOutOfBoundsException("4 >= 3"))
        }
    }
    "Size more than capacity" {
        tailArray<Int>(3).apply {
            (0..9).forEach {
                append(it * it)
            }
            size.shouldBe(10)
            shouldThrow<IndexOutOfBoundsException> { get(0) }.shouldBe(IndexOutOfBoundsException("0 < 7"))
            shouldThrow<IndexOutOfBoundsException> { get(6) }.shouldBe(IndexOutOfBoundsException("6 < 7"))
            get(7).shouldBe(49)
            get(8).shouldBe(64)
            get(9).shouldBe(81)
            shouldThrow<IndexOutOfBoundsException> { get(10) }.shouldBe(IndexOutOfBoundsException("10 >= 10"))
        }
    }
})