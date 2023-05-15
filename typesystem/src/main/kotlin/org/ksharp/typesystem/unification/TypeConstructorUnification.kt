package org.ksharp.typesystem.unification

import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.TypeConstructor

class TypeConstructorUnification : UnificationAlgo<TypeConstructor> {
    override fun unify(location: Location, typeSystem: TypeSystem, type1: TypeConstructor, type2: Type): ErrorOrType =
        typeSystem(type1).flatMap {
            typeSystem.unify(location, it, type2)
        }

}