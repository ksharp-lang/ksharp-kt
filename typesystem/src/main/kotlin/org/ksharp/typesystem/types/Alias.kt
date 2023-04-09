package org.ksharp.typesystem.types

import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeFactoryBuilder
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.annotations.Annotation

data class Alias internal constructor(
    val name: String
) : TypeVariable {
    override fun toString(): String {
        return name
    }
}

fun TypeSystem.alias(name: String): ErrorOrType =
    this[name].map {
        Alias(name)
    }


fun TypeSystemBuilder.alias(name: String, annotations: List<Annotation> = listOf(), factory: TypeFactoryBuilder) =
    item(name, annotations) {
        factory()
    }
