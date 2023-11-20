package org.ksharp.typesystem.solver

import org.ksharp.common.Either
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.Type

fun interface Solver {
    fun solve(type: Type): ErrorOrType
}

enum class Solvers(reducer: Solver) : Solver by reducer {
    NoDefined(Solver { type ->
        TODO("No defined solver for type $type")
    }),
    PassThrough(Solver { type -> Either.Right(type) }),
    Alias(AliasSolver()),
    Parametric(ParametricSolver()),
    Function(FunctionSolver()),
    Tuple(TupleSolver()),
    Union(UnionSolver()),
    UnionClass(ClassSolver()),
    ImplType(ImplTypeSolver())
}

/**
 * Return the type with the aliases solved
 */
fun Type.solve(): ErrorOrType = solver.solve(this)
