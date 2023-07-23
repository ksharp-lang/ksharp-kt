package org.ksharp.typesystem.solver

import org.ksharp.common.Either
import org.ksharp.common.cast
import org.ksharp.common.unwrap
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.ParametricType
import org.ksharp.typesystem.types.Type

class ParametricReducer : Solver {

    private fun resolveParametricType(typeSystem: TypeSystem, type: Type): ErrorOrType =
        typeSystem.solve(type).flatMap {
            if (it is ParametricType) {
                if (it.type == type) Either.Right(it.type)
                else resolveParametricType(typeSystem, it.type)
            } else resolveParametricType(typeSystem, it)
        }

    override fun reduce(typeSystem: TypeSystem, type: Type): ErrorOrType {
        val parametricType = type.cast<ParametricType>()
        return resolveParametricType(typeSystem, parametricType.type).flatMap { t ->
            parametricType.params.map { p ->
                typeSystem.solve(p)
            }.unwrap()
                .map { params ->
                    ParametricType(t.attributes + type.attributes, t, params)
                }
        }
    }

}
