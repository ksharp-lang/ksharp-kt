package org.ksharp.typesystem

import org.ksharp.common.*
import org.ksharp.typesystem.types.Type

typealias TypeEntry = Pair<String, Type>
typealias ErrorOrType = ErrorOrValue<Type>
typealias TypeFactoryBuilder = TypeItemBuilder.() -> ErrorOrType
typealias GetType = (key: String) -> Type?
typealias TypeValidation = (getType: GetType) -> Error?

class TypeItemBuilder(
    val name: String,
    private val store: Map<String, Type>,
    private val builder: PartialItemBuilder<TypeEntry>
) {
    operator fun get(key: String): Type? = store[key]

    fun isTypeNameTaken(name: String) = store.containsKey(name)

    fun validation(rule: TypeValidation) = builder.validation {
        rule(::get)
    }
}

class TypeSystemBuilder(
    private val store: MutableMap<String, Type>,
    private val builder: PartialBuilder<TypeEntry, TypeSystem>
) {

    fun item(name: String, factory: TypeFactoryBuilder) {
        builder.item {
            TypeItemBuilder(name, store, this).apply {
                validateTypeName(name).flatMap {
                    if (isTypeNameTaken(it)) {
                        Either.Left(TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to it))
                    } else {
                        factory()
                    }
                }.map { type ->
                    store[name] = type
                    value(name to type)
                }.mapLeft { error ->
                    validation { error }
                }
            }
        }
    }

    fun build() = builder.build()
}

