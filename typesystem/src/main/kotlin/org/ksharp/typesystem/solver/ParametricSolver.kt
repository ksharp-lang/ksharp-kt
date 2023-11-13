package org.ksharp.typesystem.solver

import org.ksharp.common.Either
import org.ksharp.common.cast
import org.ksharp.common.unwrap
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.ParametricType
import org.ksharp.typesystem.types.Type

class ParametricSolver : Solver {

    private fun resolveParametricType(typeSystem: TypeSystem, type: Type): ErrorOrType =
        type.solve().flatMap {
            if (it is ParametricType) {
                if (it.type == type) Either.Right(it.type)
                else resolveParametricType(typeSystem, it.type)
            } else Either.Right(it)
        }

    override fun solve(typeSystem: TypeSystem, type: Type): ErrorOrType {
        val parametricType = type.cast<ParametricType>()
        return resolveParametricType(typeSystem, parametricType.type).flatMap { t ->
            parametricType.params.map { p ->
                p.solve()
            }.unwrap()
                .map { params ->
                    ParametricType(t.typeSystem, t.attributes + type.attributes, t, params)
                }
        }
    }

}
