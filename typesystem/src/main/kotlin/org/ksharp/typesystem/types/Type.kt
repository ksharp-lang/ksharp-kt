package org.ksharp.typesystem.types

import org.ksharp.common.Either
import org.ksharp.common.ErrorOrValue
import org.ksharp.common.new
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.TypeSystemErrorCode
import org.ksharp.typesystem.annotations.Annotation
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications

interface Type {
    val serializer: TypeSerializer
    val unification: TypeUnification
    val compound: Boolean get() = true

    val terms: Sequence<Type>
    val representation: String get() = toString().let { s -> if (compound) "($s)" else s }
}

sealed interface TypeVariable : Type {
    override val compound: Boolean get() = false
    override val terms: Sequence<Type> get() = emptySequence()
}

data class Concrete internal constructor(
    val name: String
) : Type {
    override val serializer: TypeSerializer
        get() = TypeSerializers.Concrete

    override val unification: TypeUnification
        get() = TypeUnifications.NoDefined

    override val compound: Boolean get() = false
    override val terms: Sequence<Type> get() = emptySequence()
    override fun toString(): String = name
}


fun TypeItemBuilder.type(name: String): ErrorOrValue<TypeVariable> =
    Either.Right(Alias(name)).also {
        validation {
            if (it(name) == null)
                TypeSystemErrorCode.TypeNotFound.new("type" to name)
            else null
        }
    }

fun TypeSystemBuilder.type(name: String, annotations: List<Annotation> = listOf()) =
    item(name, annotations) {
        Either.Right(Concrete(name))
    }
