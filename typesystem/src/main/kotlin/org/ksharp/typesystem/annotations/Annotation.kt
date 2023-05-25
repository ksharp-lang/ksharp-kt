package org.ksharp.typesystem.annotations

data class Annotation internal constructor(
    val name: String,
    val attrs: Map<String, Any>
) {
    override fun toString(): String {
        return "@$name${
            attrs.asSequence().joinToString(" ") { (key, value) ->
                "$key=$value"
            }.takeIf { it.isNotEmpty() }?.let { "($it)" } ?: ""
        }"
    }
}
