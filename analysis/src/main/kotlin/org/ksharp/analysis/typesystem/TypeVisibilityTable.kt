package org.ksharp.analysis.typesystem

import org.ksharp.analysis.errors.ErrorCollector
import org.ksharp.common.*

enum class TypeVisibilityErrorCode(override val description: String) : ErrorCode {
    AlreadyDefined("Type already defined: {type}"),
}

enum class TypeVisibility {
    Internal,
    Public
}

class TypeVisibilityTable(private val visibility: Map<String, TypeVisibility>) {
    operator fun get(type: String): TypeVisibility? = visibility[type]

}

class TypeVisibilityTableBuilder(private val collector: ErrorCollector) {
    private val visibility = mapBuilder<String, TypeVisibility>()

    fun register(
        type: String,
        visibility: TypeVisibility,
        location: Location = Location.NoProvided
    ): ErrorOrValue<Boolean> =
        if (this.visibility.containsKey(type) != true) {
            this.visibility.put(type, visibility)
            Either.Right(true)
        } else collector.collect(
            Either.Left(TypeVisibilityErrorCode.AlreadyDefined.new(location, "type" to type))
        )

    fun build(): TypeVisibilityTable = TypeVisibilityTable(visibility.build())
}

val TypeVisibility.isInternal get() = this == TypeVisibility.Internal
val TypeVisibility.isPublic get() = this == TypeVisibility.Public
