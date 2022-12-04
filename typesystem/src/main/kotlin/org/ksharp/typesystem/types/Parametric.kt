package org.ksharp.typesystem.types

import org.ksharp.common.Either
import org.ksharp.common.ErrorOrValue
import org.ksharp.common.new
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.TypeSystemErrorCode
import org.ksharp.typesystem.validateTypeParamName

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

    fun parameter(name: String, label: String? = null) {
        result = result.flatMap { params ->
            validateTypeParamName(name).map {
                params.add(Parameter(it).labeled(label))
                params
            }
        }
    }

    fun type(name: String, label: String? = null) {
        result = result.flatMap { params ->
            builder.type(name).map {
                params.add(it.labeled(label))
                params
            }
        }
    }

    fun parametricType(name: String, label: String? = null, builder: ParametricTypeFactoryBuilder) {
        result = result.flatMap { params ->
            this.builder.parametricType(name, builder).map {
                params.add(it.labeled(label))
                params
            }
        }
    }

    fun functionType(label: String? = null, builder: ParametricTypeFactoryBuilder) {
        result = result.flatMap { params ->
            this.builder.functionType(builder).map {
                params.add(it.labeled(label))
                params
            }
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