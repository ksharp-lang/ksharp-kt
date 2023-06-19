package org.ksharp.ir

import org.ksharp.typesystem.types.Type

interface Attribute : IrNode

enum class CommonAttribute(val description: String) : Attribute {
    Native("Symbol implementation is native"),
    Public("Symbol accessible in any module"),
    Private("Symbol accessible only in the module where they are defined"),
    Impure("Symbol has side effects"),
    Constant("Symbol represent a compile constant value")
}

data class SymbolName(
    val name: String,
    val mappings: Map<String, String>
) : IrNode

data class Argument(
    val name: String,
    val type: Type
) : IrNode

interface TopLevelSymbol : IrNode {
    val targetLanguage: Set<String>
    val attributes: Set<Attribute>
    val name: SymbolName
    val expr: Expression
}

data class Function(
    override val targetLanguage: Set<String>,
    override val attributes: Set<Attribute>,
    override val name: SymbolName,
    val arguments: List<Argument>,
    override val expr: Expression
) : TopLevelSymbol


data class Type(
    override val attributes: Set<Attribute>,
    override val name: SymbolName,
    val parameters: List<String>,
    override val expr: Expression
) : TopLevelSymbol {
    override val targetLanguage: Set<String>
        get() = emptySet()
}
