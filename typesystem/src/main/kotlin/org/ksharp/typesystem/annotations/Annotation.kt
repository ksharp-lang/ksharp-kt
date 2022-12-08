package org.ksharp.typesystem.annotations

data class Annotation internal constructor(
    val name: String,
    val attrs: Map<String, Any>
)

