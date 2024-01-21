package org.ksharp.typesystem.solver

import org.ksharp.common.cast
import org.ksharp.common.unwrap
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.FullFunctionType
import org.ksharp.typesystem.types.PartialFunctionType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.toFunctionType

class FullFunctionSolver : Solver {
    override fun solve(type: Type): ErrorOrType =
        type.cast<FullFunctionType>().let { f ->
            f.arguments.map { p ->
                p.solve()
            }.unwrap()
                .map { arguments ->
                    arguments.toFunctionType(type.typeSystem, type.attributes, f.scope)
                }
        }
    
}

class PartialFunctionSolver : Solver {
    override fun solve(type: Type): ErrorOrType =
        type.cast<PartialFunctionType>()
            .arguments.map { p ->
                p.solve()
            }.unwrap()
            .map { arguments ->
                PartialFunctionType(
                    arguments,
                    type.cast<PartialFunctionType>().function
                )
            }


}
