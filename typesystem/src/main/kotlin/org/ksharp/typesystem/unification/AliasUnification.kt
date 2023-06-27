package org.ksharp.typesystem.unification

import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Type

class AliasUnification : UnificationAlgo<Type> {
    override fun unify(location: Location, typeSystem: TypeSystem, type1: Type, type2: Type): ErrorOrType =
        typeSystem(type1).flatMap {
            typeSystem.unify(location, it, type2)
        }

}
