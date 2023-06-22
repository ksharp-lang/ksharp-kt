package org.ksharp.typesystem.types

import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeFactoryBuilder
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications

data class Alias internal constructor(
    override val attributes: Set<Attribute>,
    val name: String
) : TypeVariable {
    override val serializer: TypeSerializer
        get() = TypeSerializers.Alias

    override val unification: TypeUnification
        get() = TypeUnifications.Alias

    override val substitution: Substitution
        get() = Substitutions.Alias

    override fun toString(): String {
        return name
    }
}

fun TypeSystem.alias(attributes: Set<Attribute> = NoAttributes, name: String): ErrorOrType =
    this[name].map {
        Alias(attributes, name)
    }


fun TypeSystemBuilder.alias(
    attributes: Set<Attribute>,
    name: String,
    factory: TypeFactoryBuilder
) =
    item(attributes, name) {
        factory()
    }
