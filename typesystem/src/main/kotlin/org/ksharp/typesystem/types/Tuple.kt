package org.ksharp.typesystem.types

import org.ksharp.typesystem.TypeItemBuilder

data class TupleType internal constructor(
    val elements: List<Type>,
) : Type {
    override val terms: Sequence<Type>
        get() = elements.asSequence()
    
    override val compound: Boolean = true
    override fun toString(): String = elements.asSequence().map { it.representation }.joinToString(", ")
}

fun TypeItemBuilder.tupleType(factory: ParametricTypeFactoryBuilder) =
    ParametricTypeFactory(this).apply(factory).build().map {
        TupleType(it)
    }