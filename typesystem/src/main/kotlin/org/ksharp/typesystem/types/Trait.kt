package org.ksharp.typesystem.types

import org.ksharp.common.*
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.annotations.Annotation
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications
import org.ksharp.typesystem.validateFunctionName
import org.ksharp.typesystem.validateTypeParamName

typealias TraitTypeFactoryBuilder = TraitTypeFactory.() -> Unit

interface IsTrait

data class TraitType internal constructor(
    val name: String,
    val param: String,
    val methods: Map<String, MethodType>,
) : Type, IsTrait {

    override val serializer: TypeSerializer
        get() = TypeSerializers.TraitType

    override val unification: TypeUnification
        get() = TypeUnifications.NoDefined

    data class MethodType internal constructor(
        val name: String,
        val arguments: List<Type>,
    ) : Type {

        override val serializer: TypeSerializer
            get() = TypeSerializers.MethodType

        override val unification: TypeUnification
            get() = TypeUnifications.NoDefined
        
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

    fun error(error: Error) {
        result = Either.Left(error)
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