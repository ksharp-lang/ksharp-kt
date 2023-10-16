package org.ksharp.typesystem.unification

import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.Type

class AliasUnification : UnificationAlgo<Type> {
    override fun unify(location: Location, type1: Type, type2: Type): ErrorOrType =
        type1().flatMap {
            it.unify(location, type2)
        }

}
