package org.ksharp.common

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class FlagTest : StringSpec({
    "Check flag" {
        val flag = Flag()
        flag.enabled.shouldBeFalse()
        flag.activate()
        flag.enabled.shouldBeTrue()
    }
})