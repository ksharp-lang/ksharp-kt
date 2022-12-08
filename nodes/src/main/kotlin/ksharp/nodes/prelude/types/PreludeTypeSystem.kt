package ksharp.nodes.prelude.types

import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.typeSystem
import org.ksharp.typesystem.types.parametricType

fun preludeTypeSystem(primitives: TypeSystemBuilder.() -> Unit) = typeSystem {
    primitives()
    parametricType("MutableList") {
        parameter("a")
    }
    parametricType("MutableMap") {
        parameter("k")
        parameter("v")
    }
    parametricType("MutableSet") {
        parameter("a")
    }
}