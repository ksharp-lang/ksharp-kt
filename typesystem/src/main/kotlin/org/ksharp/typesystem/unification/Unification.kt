package org.ksharp.typesystem.unification

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type

fun interface UnificationChecker {
    fun isImplemented(trait: TraitType, type: Type): Boolean
}

fun interface UnificationAlgo<T : Type> {
    fun unify(location: Location, type1: T, type2: Type, checker: UnificationChecker): ErrorOrType
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
    TypeConstructor(TypeConstructorUnification()),
    Union(UnionUnification()),
    Method(MethodUnification()),
    Trait(TraitUnification()),
    NoDefined(UnificationAlgo { _, type1, type2, _ -> TODO("Not yet implemented $type1 -- $type2") })
}

fun Type.unify(location: Location, type2: Type, checker: UnificationChecker): ErrorOrType =
    unification.algo.cast<UnificationAlgo<Type>>().unify(location, this, type2, checker)
