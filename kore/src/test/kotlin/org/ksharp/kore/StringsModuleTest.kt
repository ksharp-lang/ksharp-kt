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
    "char-at/2" {
        """
            import strings as s
            fn = s.char-at "Hello" (int 0)
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight('H')
    }
    "comparable less" {
        """
            import strings as s
            fn = "Hello" < "World"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(true)
    }
    "comparable less - false" {
        """
            import strings as s
            fn = "World" < "Hello"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(false)
    }
    "comparable greater" {
        """
            import strings as s
            fn = "World" > "Hello"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(true)
    }
    "comparable greater - false" {
        """
            import strings as s
            fn = "Hello" > "World"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(false)
    }
    "comparable equal" {
        """
            import strings as s
            fn = "World" == "World"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(true)
    }
    "comparable equal - false" {
        """
            import strings as s
            fn = "World" == "Hello"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(false)
    }
    "comparable less - equals -1" {
        """
            import strings as s
            fn = "Hello" <= "World"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(true)
    }
    "comparable less - equals - 2" {
        """
            import strings as s
            fn = "Hello" <= "Hello"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(true)
    }
    "comparable greater - equals -1" {
        """
            import strings as s
            fn = "World" >= "Hello" 
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(true)
    }
    "comparable greater - equals - 2" {
        """
            import strings as s
            fn = "Hello" >= "Hello"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(true)
    }
})
