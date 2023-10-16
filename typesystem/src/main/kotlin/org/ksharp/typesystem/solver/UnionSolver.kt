package org.ksharp.typesystem.solver

import org.ksharp.common.cast
import org.ksharp.common.unwrap
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.UnionType

class UnionSolver : Solver {
    override fun solve(typeSystem: TypeSystem, type: Type): ErrorOrType =
        type.cast<UnionType>()
            .arguments.map {
                typeSystem.solve(it.value).map { t ->
                    it.key to t
                }
            }.unwrap().map {
                UnionType(type.typeSystem, type.attributes, it.toMap().cast())
            }
}

class ClassSolver : Solver {
    override fun solve(typeSystem: TypeSystem, type: Type): ErrorOrType =
        type.cast<UnionType.ClassType>()
            .params.map {
                typeSystem.solve(it)
            }.unwrap().map {
                UnionType.ClassType(type.typeSystem, type.cast<UnionType.ClassType>().label, it)
            }
}
