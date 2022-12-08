package ksharp.nodes.prelude.types

import org.ksharp.common.Either
import org.ksharp.typesystem.types.ParametricTypeFactory
import org.ksharp.typesystem.types.Type
import kotlin.reflect.KClass

data class Interpreted internal constructor(
    val clazz: KClass<*>
) : Type {
    override val compound: Boolean = false
    override val terms: Sequence<Type> = emptySequence()
    override val representation: String = "interpreted<${clazz.qualifiedName}>"
}

fun interpreted(clazz: KClass<*>) =
    Either.Right(Interpreted(clazz))

fun ParametricTypeFactory.interpreted(clazz: KClass<*>) =
    add { Either.Right(Interpreted(clazz)) }