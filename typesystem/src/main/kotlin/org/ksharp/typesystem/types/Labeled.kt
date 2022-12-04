package org.ksharp.typesystem.types

data class Labeled internal constructor(
    val label: String,
    val type: Type
) : Type by type {
    override val terms: Sequence<Type>
        get() = sequenceOf(type)
    
    override val representation: String
        get() = toString()

    override fun toString(): String = "$label: $type"
}

internal fun Type.labeled(label: String?) = label?.let { Labeled(it, this) } ?: this
