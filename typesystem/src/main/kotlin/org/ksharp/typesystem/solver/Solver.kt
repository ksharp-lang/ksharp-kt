package org.ksharp.typesystem.solver

import org.ksharp.common.Either
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Type

fun interface Solver {
    fun solve(typeSystem: TypeSystem, type: Type): ErrorOrType
}

enum class Solvers(reducer: Solver) : Solver by reducer {
    NoDefined(Solver { _, type ->
        TODO("No defined solver for type $type")
    }),
    PassThrough(Solver { _, type -> Either.Right(type) }),
    Alias(AliasSolver()),
    Parametric(ParametricSolver()),
    Function(FunctionSolver()),
    Tuple(TupleSolver()),
    Union(UnionSolver()),
    UnionClass(ClassSolver()),
}

/**
 * Return the type with the aliases solved
 */
fun Type.solve(): ErrorOrType = solver.solve(this.typeSystem.handle!!, this)
