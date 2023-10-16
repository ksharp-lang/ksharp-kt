package org.ksharp.typesystem

import org.ksharp.common.*
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.TypeAlias

typealias TypeEntry = Pair<String, Type>
typealias ErrorOrType = ErrorOrValue<Type>
typealias TypeFactoryBuilder = TypeItemBuilder.() -> ErrorOrType
typealias GetType = (key: String) -> Type?
typealias TypeValidation = (getType: GetType) -> Error?

class TypeItemBuilder(
    val handle: HandlePromise<TypeSystem>,
    val attributes: Set<Attribute>,
    val name: String,
    private val storeView: MapView<String, Type>,
    private val store: MapBuilder<String, Type>,
    private val builder: PartialItemBuilder<TypeEntry>,
    private val partialBuilder: PartialBuilder<TypeEntry, TypeSystem>
) {
    operator fun get(key: String): Type? = storeView[key]?.let {
        if (it is TypeAlias) {
            get(it.name)
        } else it
    }

    fun isTypeNameTaken(name: String) = storeView.containsKey(name)!!

    fun validation(rule: TypeValidation) = builder.validation {
        rule(::get)
    }

    fun add(name: String, type: Type) =
        validateTypeName(name).flatMap {
            if (isTypeNameTaken(it)) {
                Either.Left(TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to it))
            } else {
                Either.Right(type)
            }
        }.map { t ->
            store.put(name, t)
            partialBuilder.item {
                value(name to t)
            }
        }.mapLeft { error ->
            validation { error }
        }

    internal fun createForSubtypes() =
        TypeItemBuilder(handle, NoAttributes, name, storeView, store, builder, partialBuilder)

}

class TypeSystemBuilder(
    private val parent: TypeSystem?,
    val handle: HandlePromise<TypeSystem>,
    private val store: MapBuilder<String, Type>,
    private val builder: PartialBuilder<TypeEntry, TypeSystem>
) {

    private val mapView = object : MapView<String, Type> {
        private fun getFromParent(key: String): Type? =
            if (parent != null) parent[key].valueOrNull
            else null

        override fun get(key: String): Type? =
            store.get(key) ?: getFromParent(key)

        override fun containsKey(key: String): Boolean =
            store.containsKey(key) == true || (getFromParent(key) != null)
    }

    fun item(
        attributes: Set<Attribute>,
        name: String,
        factory: TypeFactoryBuilder
    ) {
        builder.item {
            TypeItemBuilder(handle, attributes, name, mapView, store, this, builder).apply {
                validateTypeName(name).flatMap {
                    if (isTypeNameTaken(it)) {
                        Either.Left(TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to it))
                    } else {
                        factory()
                    }
                }.map { type ->
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
