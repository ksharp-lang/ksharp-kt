package org.ksharp.typesystem.solver

import org.ksharp.common.Either
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Type

fun interface Solver {
    fun reduce(typeSystem: TypeSystem, type: Type): ErrorOrType
}

enum class Solvers(reducer: Solver) : Solver by reducer {
    NoDefined(Solver { _, type ->
        TODO("No defined reducer for type $type")
    }),
    PassThrough(Solver { _, type -> Either.Right(type) }),
    Alias(AliasReducer()),
    Parametric(ParametricReducer()),
}

fun TypeSystem.solve(type: Type): ErrorOrType = type.solver.reduce(this, type)
