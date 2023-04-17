package org.ksharp.semantics.nodes

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.cast
import org.ksharp.module.prelude.preludeModule
import org.ksharp.semantics.inference.ErrorTypePromise
import org.ksharp.semantics.inference.getTypePromise
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.types.Type

class SemanticInfoTest : StringSpec({
    val ts = preludeModule.typeSystem
    val longTypePromise = ts.getTypePromise("Long")
    "List<SemanticInfo> to List<Types>" {
        listOf<SemanticInfo>(
            TypeSemanticInfo(longTypePromise),
            TypeSemanticInfo(longTypePromise),
        ).types.shouldBeRight(
            listOf<Type>(
                ts["Long"].valueOrNull!!,
                ts["Long"].valueOrNull!!
            )
        )
    }
    "List<SemanticInfo> with Error to List<Types>" {
        listOf<SemanticInfo>(
            TypeSemanticInfo(longTypePromise),
            TypeSemanticInfo(ts.getTypePromise("TypeNotFound")),
        ).types.shouldBeLeft(
            ts.getTypePromise("TypeNotFound").cast<ErrorTypePromise>().error
        )
    }
})