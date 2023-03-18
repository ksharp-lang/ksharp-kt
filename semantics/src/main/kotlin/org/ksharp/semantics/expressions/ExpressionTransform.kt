package org.ksharp.semantics.expressions

import org.ksharp.nodes.ExpressionParserNode
import org.ksharp.nodes.LiteralValueNode
import org.ksharp.nodes.LiteralValueType
import org.ksharp.nodes.semantic.ConstantNode
import org.ksharp.nodes.semantic.SemanticNode
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.semantics.nodes.getTypeSemanticInfo
import org.ksharp.typesystem.TypeSystem

private fun LiteralValueType.toSemanticInfo(typeSystem: TypeSystem) =
    when (this) {
        LiteralValueType.Integer,
        LiteralValueType.HexInteger,
        LiteralValueType.OctalInteger,
        LiteralValueType.BinaryInteger -> typeSystem.getTypeSemanticInfo("Long")

        LiteralValueType.Decimal -> typeSystem.getTypeSemanticInfo("Double")
        LiteralValueType.String -> typeSystem.getTypeSemanticInfo("String")
        LiteralValueType.MultiLineString -> typeSystem.getTypeSemanticInfo("String")
        LiteralValueType.Character -> typeSystem.getTypeSemanticInfo("Char")
        else -> TODO()
    }

private fun String.toValue(type: LiteralValueType): Any =
    when (type) {
        LiteralValueType.Integer -> toLong()
        LiteralValueType.HexInteger -> substring(2).toLong(16)
        LiteralValueType.OctalInteger -> substring(2).toLong(8)
        LiteralValueType.BinaryInteger -> substring(2).toLong(2)

        LiteralValueType.Decimal -> toDouble()
        LiteralValueType.String -> substring(1, length - 1)
        LiteralValueType.MultiLineString -> substring(3, length - 3)
        LiteralValueType.Character -> substring(1, length - 1)[0]
        else -> TODO()
    }

internal fun ExpressionParserNode.toSemanticNode(
    info: SemanticInfo,
    typeSystem: TypeSystem
): SemanticNode<SemanticInfo> =
    when (this) {
        is LiteralValueNode -> ConstantNode(
            value.toValue(type),
            type.toSemanticInfo(typeSystem),
            location
        )

        else -> TODO()
    }