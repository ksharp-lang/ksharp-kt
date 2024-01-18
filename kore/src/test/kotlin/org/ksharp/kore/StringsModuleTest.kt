package org.ksharp.kore

import io.kotest.core.spec.style.StringSpec
import org.ksharp.test.shouldBeRight

class StringsModuleTest : StringSpec({
    "index/2" {
        """
            import strings as s
            fn = s.index "Hello" (int 1)
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight('e')
    }
    "length/1" {
        """
            import strings as s
            fn = s.length "Hello"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(5)
    }
    "comparable" {
        """
            import strings as s
            fn = "Hello" < "World"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(true)
    }
})
