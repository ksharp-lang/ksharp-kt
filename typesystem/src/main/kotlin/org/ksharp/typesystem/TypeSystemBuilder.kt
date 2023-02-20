package org.ksharp.typesystem

import org.ksharp.common.*
import org.ksharp.typesystem.annotations.Annotation
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.annotated

typealias TypeEntry = Pair<String, Type>
typealias ErrorOrType = ErrorOrValue<Type>
typealias TypeFactoryBuilder = TypeItemBuilder.() -> ErrorOrType
typealias GetType = (key: String) -> Type?
typealias TypeValidation = (getType: GetType) -> Error?

class TypeItemBuilder(
    val name: String,
    private val store: MapView<String, Type>,
    private val builder: PartialItemBuilder<TypeEntry>
) {
    operator fun get(key: String): Type? = store[key]

    fun isTypeNameTaken(name: String) = store.containsKey(name)!!

    fun validation(rule: TypeValidation) = builder.validation {
        rule(::get)
    }
}

class TypeSystemBuilder(
    private val parent: TypeSystem?,
    private val store: MapBuilder<String, Type>,
    private val builder: PartialBuilder<TypeEntry, TypeSystem>
) {

    private val mapView = object : MapView<String, Type> {

        override fun get(key: String): Type? =
            store.get(key) ?: parent?.get(key)?.valueOrNull

        override fun containsKey(key: String): Boolean =
            if (store.containsKey(key) == true) true else (parent?.get(key)?.valueOrNull != null)

    }

    fun item(
        name: String,
        annotations: List<Annotation>,
        factory: TypeFactoryBuilder
    ) {
        builder.item {
            TypeItemBuilder(name, mapView, this).apply {
                validateTypeName(name).flatMap {
                    if (isTypeNameTaken(it)) {
                        Either.Left(TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to it))
                    } else {
                        factory()
                    }
                }.map { t ->
                    val type = if (annotations.isEmpty()) {
                        t
                    } else t.annotated(annotations)
                    store.put(name, type)
                    value(name to type)
                }.mapLeft { error ->
                    validation { error }
                }
            }
        }
    }

    fun build() = builder.build()
}

