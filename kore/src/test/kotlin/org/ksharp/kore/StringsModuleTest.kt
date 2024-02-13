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
    "lowercase" {
        """
            import strings as s
            fn = s.lowercase "Hello"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight("hello")
    }
    "uppercase" {
        """
            import strings as s
            fn = s.uppercase "Hello"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight("HELLO")
    }
    "trim" {
        """
            import strings as s
            fn = s.trim "   Hello    "
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight("Hello")
    }
    "starts-with" {
        """
            import strings as s
            fn = s.starts-with? "Hello World" "Hello"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(true)
    }
    "starts-with 2" {
        """
            import strings as s
            fn = s.starts-with? "Hello World" "World"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(false)
    }
    "ends-with" {
        """
            import strings as s
            fn = s.ends-with? "Hello World" "World"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(true)
    }
    "ends-with 2" {
        """
            import strings as s
            fn = s.ends-with? "Hello World" "Hello"
        """.trimIndent()
            .evaluateFunction("fn/0")
            .shouldBeRight(false)
    }
})
