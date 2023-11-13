package org.ksharp.typesystem.solver

import org.ksharp.common.cast
import org.ksharp.common.unwrap
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.toFunctionType

class FunctionSolver : Solver {
    override fun solve(type: Type): ErrorOrType =
        type.cast<FunctionType>()
            .arguments.map { p ->
                p.solve()
            }.unwrap()
            .map { arguments ->
                arguments.toFunctionType(type.typeSystem, type.attributes)
            }


}
