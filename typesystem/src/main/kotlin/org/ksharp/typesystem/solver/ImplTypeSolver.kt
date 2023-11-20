package org.ksharp.typesystem.solver

import org.ksharp.common.cast
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.ImplType
import org.ksharp.typesystem.types.Type

class ImplTypeSolver : Solver {
    override fun solve(type: Type): ErrorOrType =
        type.cast<ImplType>().impl.solve()
    
}
