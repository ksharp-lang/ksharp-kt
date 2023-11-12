package org.ksharp.typesystem.unification

import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.TypeConstructor

class TypeConstructorUnification : UnificationAlgo<TypeConstructor> {
    override fun unify(
        location: Location,
        type1: TypeConstructor,
        type2: Type,
        checker: UnificationChecker
    ): ErrorOrType =
        type1().flatMap {
            it.unify(location, type2, checker)
        }

}
