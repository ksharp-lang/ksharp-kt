package org.ksharp.typesystem.types

import org.ksharp.common.Either
import org.ksharp.common.ErrorOrValue
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.validateTypeName

typealias UnionTypeFactoryBuilder = UnionTypeFactory.() -> Unit

data class UnionType internal constructor(
    val arguments: Map<String, ClassType>,
) : Type {
    override val compound: Boolean = false
    override fun toString(): String =
        arguments.asSequence().map { (_, value) -> value.representation }
            .joinToString("\n|")

    data class ClassType internal constructor(
        val label: String,
        val params: List<Type>
    ) : Type {
        override val compound: Boolean = false
        override fun toString(): String = "$label${if (params.isEmpty()) "" else " "}${
            params.asSequence().map { it.representation }.joinToString(" ")
        }"
    }
}

class UnionTypeFactory(
    private val factory: TypeItemBuilder
) {
    private var result: ErrorOrValue<MutableMap<String, UnionType.ClassType>> = Either.Right(mutableMapOf())

    fun clazz(label: String, parameters: ParametricTypeFactoryBuilder = {}) {
        result = result.flatMap { params ->
            validateTypeName(label).flatMap {
                ParametricTypeFactory(factory).apply(parameters).build().map { args ->
                    params[label] = UnionType.ClassType(
                        label = label,
                        params = args
                    )
                    params
                }
            }
        }
    }

    internal fun build() = result.map { it.toMap() }
}

fun TypeItemBuilder.unionType(factory: UnionTypeFactoryBuilder) =
    UnionTypeFactory(this).apply(factory).build().map {
        UnionType(it)
    }