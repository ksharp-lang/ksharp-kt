package org.ksharp.module

import org.ksharp.typesystem.types.Type

interface TraitInfo {
    val name: String
    val definitions: Map<String, Type>
    val implementations: Map<String, FunctionInfo>
}

internal data class TraitInfoImpl(
    override val name: String,
    override val definitions: Map<String, Type>,
    override val implementations: Map<String, FunctionInfo>
) : TraitInfo

fun traitInfo(name: String, definitions: Map<String, Type>, implementations: Map<String, FunctionInfo>): TraitInfo =
    TraitInfoImpl(name, definitions, implementations)
