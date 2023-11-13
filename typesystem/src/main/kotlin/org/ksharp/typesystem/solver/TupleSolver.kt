package org.ksharp.typesystem.solver

import org.ksharp.common.cast
import org.ksharp.common.unwrap
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.TupleType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.toTupleType

class TupleSolver : Solver {
    override fun solve(typeSystem: TypeSystem, type: Type): ErrorOrType =
        type.cast<TupleType>()
            .elements.map { p ->
                p.solve()
            }.unwrap()
            .map { elements ->
                elements.toTupleType(typeSystem, type.attributes)
            }


}
