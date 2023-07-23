package org.ksharp.typesystem.reducer

import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Type

fun interface Reducer {
    fun reduce(typeSystem: TypeSystem, type: Type): Type
}

enum class Reducers(reducer: Reducer) : Reducer by reducer {
    NoDefined(Reducer { _, type ->
        TODO("No defined reducer for type $type")
    }),
    Passthrough(Reducer { _, type -> type })
}

fun TypeSystem.reduce(type: Type): Type = type.reducer.reduce(this, type)
