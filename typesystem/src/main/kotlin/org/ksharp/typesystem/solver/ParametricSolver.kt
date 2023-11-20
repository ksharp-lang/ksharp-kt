package org.ksharp.typesystem.solver

import org.ksharp.common.Either
import org.ksharp.common.cast
import org.ksharp.common.unwrap
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.ParametricType
import org.ksharp.typesystem.types.Type

class ParametricSolver : Solver {

    private fun resolveParametricType(type: Type): ErrorOrType =
        type.solve().flatMap {
            if (it is ParametricType) {
                if (it.type == type) Either.Right(it.type)
                else resolveParametricType(it.type)
            } else Either.Right(it)
        }

    override fun solve(type: Type): ErrorOrType {
        val parametricType = type.cast<ParametricType>()
        return resolveParametricType(parametricType.type).flatMap { t ->
            parametricType.params.map { p ->
                p.solve()
            }.unwrap()
                .map { params ->
                    ParametricType(t.typeSystem, t.attributes + type.attributes, t, params)
                }
        }
    }

}
