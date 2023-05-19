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
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications

enum class TypeVisibility {
    Internal,
    Public
}

interface Type {
    val serializer: TypeSerializer
    val unification: TypeUnification
    val substitution: Substitution
    val compound: Boolean get() = true
    val terms: Sequence<Type>
    val visibility: TypeVisibility
    val representation: String get() = toString().let { s -> if (compound) "($s)" else s }
}

sealed interface TypeVariable : Type {
    override val compound: Boolean get() = false
    override val terms: Sequence<Type> get() = emptySequence()
}

data class Concrete internal constructor(
    override val visibility: TypeVisibility,
    val name: String,
) : Type {
    override val serializer: TypeSerializer
        get() = TypeSerializers.Concrete

    override val unification: TypeUnification
        get() = TypeUnifications.Default

    override val substitution: Substitution
        get() = Substitutions.Identity

    override val compound: Boolean get() = false
    override val terms: Sequence<Type> get() = emptySequence()
    override fun toString(): String = name
}


fun TypeItemBuilder.type(name: String): ErrorOrValue<TypeVariable> =
    Either.Right(Alias(visibility, name)).also {
        validation {
            if (it(name) == null)
                TypeSystemErrorCode.TypeNotFound.new("type" to name)
            else null
        }
    }

fun TypeSystemBuilder.type(visibility: TypeVisibility, name: String, annotations: List<Annotation> = listOf()) =
    item(visibility, name, annotations) {
        Either.Right(Concrete(visibility, name))
    }
