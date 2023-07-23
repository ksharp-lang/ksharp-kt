package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.reducer.reduce
import org.ksharp.typesystem.types.Concrete
import org.ksharp.typesystem.types.newParameter

class ReducerTest : StringSpec({
    "reduce param" {
        val ts = typeSystem { }.value
        val param = newParameter()
        ts.reduce(param).shouldBe(param)
    }
    "reduce concrete type" {
        val ts = typeSystem { }.value
        val type = Concrete(NoAttributes, "Int")
        ts.reduce(type).shouldBe(type)
    }
})
