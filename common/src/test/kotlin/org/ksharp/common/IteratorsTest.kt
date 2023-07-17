package org.ksharp.common

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe

class IteratorsTest : StringSpec({
    "Test generateIterator" {
        val iter = listOf(1, 2, 3, 4).iterator()
        generateIterator { if (iter.hasNext()) iter.next() * 2 else null }
            .asSequence().toList().shouldContainAll(2, 4, 6, 8)
    }
    "Test combining generateIterator and flatten them" {
        val list = listOf(1, 2, 3, 4)
        val iter1 = list.iterator()
        val iter2 = list.iterator()
        sequenceOf(
            generateIterator { if (iter1.hasNext()) iter1.next() * 2 else null }
                .asSequence(),
            generateIterator { if (iter2.hasNext()) iter2.next() * 3 else null }
                .asSequence()
        ).flatten()
            .toList().shouldBe(listOf(2, 4, 6, 8, 3, 6, 9, 12))
    }
    "Test calling next before hasNext" {
        val iter = listOf(1, 2, 3, 4).iterator()
        generateIterator { if (iter.hasNext()) iter.next() * 2 else null }.apply {
            shouldThrow<NoSuchElementException>(){
                next()
            }
        }
    }
})
