package org.ksharp.semantics.expressions

import org.ksharp.common.cast
import org.ksharp.nodes.*
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.MaybePolymorphicTypePromise
import org.ksharp.semantics.nodes.*
import org.ksharp.semantics.scopes.SymbolTable
import org.ksharp.semantics.scopes.SymbolTableBuilder
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
        else -> TODO("Literal value type $this")
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
        else -> TODO("Literal value type $type: $this")
    }

internal fun ExpressionParserNode.toSemanticNode(
    errors: ErrorCollector,
    info: SemanticInfo,
    typeSystem: TypeSystem
): SemanticNode<SemanticInfo> =
    when (this) {
        is LiteralValueNode -> ConstantNode(
            value.toValue(type),
            type.toSemanticInfo(typeSystem),
            location
        )

        is OperatorNode -> ApplicationNode(
            "($operator)",
            listOf(
                left.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                right.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem)
            ),
            TypeSemanticInfo(
                MaybePolymorphicTypePromise("app-return")
            ),
            location
        )

        is IfNode -> ApplicationNode(
            "if",
            listOf(
                condition.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                trueExpression.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                falseExpression.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem)
            ),
            TypeSemanticInfo(
                MaybePolymorphicTypePromise("if-return")
            ),
            location
        )

        is FunctionCallNode -> if (arguments.isEmpty()) {
            VarNode(
                name,
                info.cast<SymbolResolver>().getSymbol(name)
                    ?: TypeSemanticInfo(MaybePolymorphicTypePromise(name)),
                location
            )
        } else TODO("Call with parameters")

        is UnitNode -> ConstantNode(
            Unit,
            typeSystem.getTypeSemanticInfo("Unit"),
            location
        )

        is LetExpressionNode -> {
            val table = SymbolTableBuilder(info.cast<SymbolTable>(), errors)
            val letInfo = LetSemanticInfo(table)
            LetNode(
                listOf(),
                expression.cast<ExpressionParserNode>().toSemanticNode(errors, letInfo, typeSystem),
                EmptySemanticInfo,
                location
            )
        }

        else -> TODO("No supported $this")
    }