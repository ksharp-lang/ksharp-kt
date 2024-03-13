package org.ksharp.common

/**
 * This interface encode a error.
 *
 * 1. name: represent a code that is resolved using i18n.
 * 2. description: default language error description.
 *
 * A common pattern is encode the errors using enumerations
 *
 * ```kotlin
 * enum class TypeSystemError(override val description: String) : ErrorCode {
 * TypeSystem001("The typename should start with a uppercase character")
 * }
 * ```
 */
interface ErrorCode {
    val name: String
    val description: String
}

data class Error(
    val code: ErrorCode,
    val location: Location?,
    val arguments: Map<String, Any> = mapOf()
) {
    val representation get() = "${toString()} :${location?.start?.first?.value}"

    override fun toString(): String {
        val description = arguments.entries.fold(code.description) { description, entry ->
            description.replace("{${entry.key}}", entry.value.toString())
        }
        return "${code.name}: $description"
    }
}

typealias ErrorOrValue<R> = Either<Error, R>

fun ErrorCode.new(location: Location?, vararg arguments: Pair<String, Any>) =
    Error(this, location, arguments.toMap())

fun ErrorCode.new(vararg arguments: Pair<String, Any>) =
    this.new(location = null, *arguments)
