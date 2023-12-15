package org.ksharp.compiler

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.compiler.loader.CyclingReferences

class CyclingReferencesTest : StringSpec({
    "Base cycling" {
        val cycling = CyclingReferences()
        cycling.loading("a", "b")
            .shouldBeEmpty()
        cycling.pending.toList().shouldBe(listOf("a"))
        cycling.loaded("a")
        cycling.pending.toList().shouldBeEmpty()
    }
    "Cycling reference" {
        val cycling = CyclingReferences()
        cycling.loading("a", "")
            .shouldBeEmpty()
        cycling.loading("b", "a")
            .shouldBeEmpty()
        cycling.loading("a", "b")
            .shouldBe(setOf("b"))
    }
})
