package org.ksharp.typesystem.solver

import org.ksharp.common.Either
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Alias
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.TypeAlias

class AliasSolver : Solver {
    override fun solve(typeSystem: TypeSystem, type: Type): ErrorOrType =
        type().flatMap {
            if (it is Alias || it is TypeAlias) {
                it.solve()
            } else Either.Right(it)
        }

}
