package org.ksharp.typesystem.unification

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Type

interface UnificationAlgo<T : Type> {
    fun unify(location: Location, typeSystem: TypeSystem, type1: T, type2: Type): ErrorOrType
}

interface TypeUnification {
    val algo: UnificationAlgo<out Type>
}

enum class TypeUnifications(override val algo: UnificationAlgo<out Type>) : TypeUnification {
    Alias(AliasUnification()),
    Default(DefaultUnification()),
    Function(FunctionUnification()),
    Parameter(ParameterUnification()),
    Parametric(ParametricUnification()),
    Tuple(TupleUnification()),
    NoDefined(object : UnificationAlgo<Type> {
        override fun unify(location: Location, typeSystem: TypeSystem, type1: Type, type2: Type): ErrorOrType {
            TODO("Not yet implemented")
        }
    })
}

fun TypeSystem.unify(location: Location, type1: Type, type2: Type): ErrorOrType =
    type1.unification.algo.cast<UnificationAlgo<Type>>().unify(location, this, type1, type2)