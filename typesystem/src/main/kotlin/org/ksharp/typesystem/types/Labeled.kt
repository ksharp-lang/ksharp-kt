package org.ksharp.typesystem.types

import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers

data class Labeled internal constructor(
    val label: String,
    val type: Type
) : Type by type {

    override val serializer: TypeSerializer
        get() = TypeSerializers.Labeled

    override val terms: Sequence<Type>
        get() = sequenceOf(type)

    override val representation: String
        get() = toString()

    override fun toString(): String = "$label: ${type.representation}"
}

internal fun Type.labeled(label: String?) = label?.let { Labeled(it, this) } ?: this

val Type.label: String?
    get() = when (this) {
        is Labeled -> label
        else -> null
    }