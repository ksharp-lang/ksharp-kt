package org.ksharp.typesystem.solver

import org.ksharp.common.Either
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Alias
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.TypeAlias

class AliasReducer : Solver {
    override fun reduce(typeSystem: TypeSystem, type: Type): ErrorOrType =
        typeSystem(type).flatMap {
            if (it is Alias || it is TypeAlias) {
                typeSystem.solve(it)
            } else Either.Right(it)
        }

}
