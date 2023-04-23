package org.ksharp.typesystem.unification

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.Type

class ParameterUnification : UnificationAlgo<Parameter> {
    override fun unify(location: Location, typeSystem: TypeSystem, type1: Parameter, type2: Type): ErrorOrType =
        Either.Right(type2)

}