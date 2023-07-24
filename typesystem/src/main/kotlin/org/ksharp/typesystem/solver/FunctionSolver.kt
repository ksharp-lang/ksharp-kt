package org.ksharp.typesystem.solver

import org.ksharp.common.cast
import org.ksharp.common.unwrap
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.toFunctionType

class FunctionSolver : Solver {
    override fun solve(typeSystem: TypeSystem, type: Type): ErrorOrType =
        type.cast<FunctionType>()
            .arguments.map { p ->
                typeSystem.solve(p)
            }.unwrap()
            .map { arguments ->
                arguments.toFunctionType(type.attributes)
            }


}
