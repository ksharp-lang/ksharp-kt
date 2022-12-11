package org.ksharp.typesystem.types

import org.ksharp.common.*
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.annotations.Annotation
import org.ksharp.typesystem.validateFunctionName
import org.ksharp.typesystem.validateTypeParamName

typealias TraitTypeFactoryBuilder = TraitTypeFactory.() -> Unit

data class TraitType internal constructor(
    val name: String,
    val param: String,
    val methods: Map<String, MethodType>,
) : Type {

    data class MethodType internal constructor(
        val name: String,
        val arguments: List<Type>,
    ) : Type {

        override val compound: Boolean
            get() = false
        override val terms: Sequence<Type>
            get() = arguments.asSequence()

        override fun toString(): String =
            "$name :: ${arguments.joinToString(" -> ") { it.representation }}"
    }

    override val compound: Boolean
        get() = false

    override val terms: Sequence<Type>
        get() = methods.values.asSequence()

    override fun toString(): String = """
        |trait $name $param =
        |    ${methods.values.joinToString("\n    ") { it.representation }}
    """.trimMargin("|")
}

class TraitTypeFactory(
    private val factory: TypeItemBuilder
) {
    private var result: ErrorOrValue<MapBuilder<String, TraitType.MethodType>> = Either.Right(mapBuilder())

    fun method(name: String, arguments: ParametricTypeFactoryBuilder = {}) {
        result = result.flatMap { params ->
            validateFunctionName(name).flatMap {
                ParametricTypeFactory(factory).apply(arguments).build().map { args ->
                    params.put(
                        name, TraitType.MethodType(
                            name,
                            args
                        )
                    )
                    params
                }
            }
        }
    }

    internal fun build() = result.map { it.build() }
}

fun TypeSystemBuilder.trait(
    name: String,
    paramName: String,
    annotations: List<Annotation> = listOf(),
    factory: TraitTypeFactoryBuilder
) =
    item(name, annotations) {
        validateTypeParamName(paramName).flatMap {
            TraitTypeFactory(this).apply(factory).build().map {
                TraitType(name, paramName, it)
            }
        }
    }