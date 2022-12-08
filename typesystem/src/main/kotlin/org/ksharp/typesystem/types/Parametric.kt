package org.ksharp.typesystem.types

import org.ksharp.common.Either
import org.ksharp.common.ErrorOrValue
import org.ksharp.common.new
import org.ksharp.typesystem.*

typealias ParametricTypeFactoryBuilder = ParametricTypeFactory.() -> Unit

data class Parameter internal constructor(
    val name: String,
) : TypeVariable {
    override fun toString(): String = name
}

data class ParametricType internal constructor(
    val type: TypeVariable,
    val params: List<Type>
) : Type {
    override val terms: Sequence<Type>
        get() = sequenceOf(sequenceOf(type), params.asSequence()).flatten()

    override fun toString(): String = "$type ${params.asSequence().map { it.representation }.joinToString(" ")}"
}


class ParametricTypeFactory(
    private val builder: TypeItemBuilder
) {
    private var result: ErrorOrValue<MutableList<Type>> = Either.Right(mutableListOf())

    fun add(typeFactory: TypeFactoryBuilder) {
        result = result.flatMap { params ->
            val type = builder.typeFactory()
            type.map {
                params.add(it)
                params
            }
        }
    }

    fun parameter(name: String, label: String? = null) = add {
        validateTypeParamName(name).map {
            Parameter(it).labeled(label)
        }
    }

    fun type(name: String, label: String? = null) = add {
        builder.type(name).map {
            it.labeled(label)
        }
    }

    fun parametricType(name: String, label: String? = null, builder: ParametricTypeFactoryBuilder) = add {
        this@ParametricTypeFactory.builder.parametricType(name, builder).map {
            it.labeled(label)
        }
    }

    fun functionType(label: String? = null, builder: ParametricTypeFactoryBuilder) = add {
        this@ParametricTypeFactory.builder.functionType(builder).map {
            it.labeled(label)
        }
    }

    internal fun build(): ErrorOrValue<List<Type>> = result.map { it.toList() }
}

fun TypeItemBuilder.parametricType(name: String, factory: ParametricTypeFactoryBuilder) =
    if (name == this.name) {
        ParametricTypeFactory(this).apply(factory).build().flatMap {
            if (it.isEmpty()) {
                Either.Left(
                    TypeSystemErrorCode.ParametricTypeWithoutParameters.new("type" to name)
                )
            } else
                Either.Right(ParametricType(Concrete(name), it))
        }
    } else
        type(name).flatMap { pType ->
            validation {
                if (it(name) !is ParametricType) {
                    TypeSystemErrorCode.NoParametrizedType.new("type" to pType.representation)
                } else null
            }
            ParametricTypeFactory(this).apply(factory).build().flatMap { types ->
                validation {
                    val type = it(name)
                    if (type is ParametricType && type.params.size != types.size) {
                        TypeSystemErrorCode.InvalidNumberOfParameters.new(
                            "type" to type,
                            "number" to type.params.size,
                            "configuredType" to ParametricType(type.type, types)
                        )
                    } else null
                }
                Either.Right(ParametricType(pType, types))
            }
        }


fun TypeSystemBuilder.parametricType(name: String, factory: ParametricTypeFactoryBuilder) =
    item(name) {
        this.parametricType(name, factory)
    }