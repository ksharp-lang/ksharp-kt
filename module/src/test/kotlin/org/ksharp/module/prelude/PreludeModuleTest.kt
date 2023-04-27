package org.ksharp.module.prelude

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import org.ksharp.module.FunctionInfo

private val FunctionInfo.representation: String
    get() = "$name :: ${
        types.joinToString(" -> ") {
            it.representation
        }
    }"

class PreludeModuleTest : StringSpec({
    "Test prelude module" {
        preludeModule.functions.values
            .flatten()
            .map { it.representation }
            .shouldBeEmpty()
    }
})