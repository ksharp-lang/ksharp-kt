package org.ksharp.nodes.semantic

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.types.newParameterForTesting

class SemanticNodeTest : StringSpec({
    "Test semantic info" {
        EmptySemanticInfo()
            .apply {
                getInferredType(Location.NoProvided)
                    .shouldBeLeft(SemanticInfoErrorCode.TypeNotInferred.new(Location.NoProvided))
                getType(Location.NoProvided)
                    .shouldBeLeft(SemanticInfoErrorCode.TypeNotInferred.new(Location.NoProvided))
            }

        EmptySemanticInfo()
            .apply {
                setInferredType(Either.Right(newParameterForTesting(1)))
                getInferredType(Location.NoProvided)
                    .shouldBeRight(newParameterForTesting(1))
                getType(Location.NoProvided)
                    .shouldBeRight(newParameterForTesting(1))
            }
    }
})
