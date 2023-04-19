package org.ksharp.typesystem.unification

import org.ksharp.typesystem.types.Type

interface UnificationAlgo {
    fun unify(type1: Type, type2: Type): Type
}

interface TypeUnification {
    val algo: UnificationAlgo
}

enum class TypeUnifications(override val algo: UnificationAlgo) : TypeUnification {
    NoDefined(object : UnificationAlgo {
        override fun unify(type1: Type, type2: Type): Type {
            TODO("Not yet implemented")
        }
    })
}