package org.ksharp.typesystem.solver

import org.ksharp.common.cast
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.*

class ImplTypeSolver : Solver {
    override fun solve(type: Type): ErrorOrType =
        type.cast<ImplType>().let { t ->
            t.impl.solve().map {
                when (it) {
                    is TraitType -> FixedTraitType(t.trait)
                    is ParametricType -> if (it.type is TraitType) FixedTraitType(t.trait) else ImplType(t.trait, it)
                    is FixedTraitType -> FixedTraitType(t.trait)
                    else -> ImplType(t.trait, it)
                }
            }
        }


}
