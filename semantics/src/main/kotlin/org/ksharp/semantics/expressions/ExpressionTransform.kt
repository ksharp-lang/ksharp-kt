package org.ksharp.semantics.expressions

import org.ksharp.common.ErrorCode
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.nodes.*
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.paramTypePromise
import org.ksharp.semantics.nodes.*
import org.ksharp.semantics.scopes.SymbolTable
import org.ksharp.semantics.scopes.SymbolTableBuilder
import org.ksharp.typesystem.TypeSystem

enum class ExpressionSemanticsErrorCode(override val description: String) : ErrorCode {
    SymbolAlreadyUsed("Symbol already used '{name}'")
}

private val LiteralCollectionType.applicationName
    get(): ApplicationName =
        when (this) {
            LiteralCollectionType.List -> ApplicationName("::prelude", "listOf")
            LiteralCollectionType.Map -> ApplicationName("::prelude", "mapOf")
            LiteralCollectionType.Tuple -> ApplicationName("::prelude", "tupleOf")
            LiteralCollectionType.Set -> ApplicationName("::prelude", "setOf")
        }

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

private fun SemanticInfo.getVarSemanticInfo(name: String, location: Location): SemanticInfo =
    if (this is MatchSemanticInfo) {
        TODO()
//        table.register(name, paramTypePromise(), location)
//        table[name]?.first ?: TypeSemanticInfo(
//            ErrorTypePromise(
//                ExpressionSemanticsErrorCode.SymbolAlreadyUsed.new(location, "name" to name)
//            )
//        )
    } else cast<SymbolResolver>().getSymbol(name)
        ?: TypeSemanticInfo(paramTypePromise())

private fun SemanticInfo.callSemanticInfo(): SemanticInfo =
    if (this is MatchSemanticInfo) {
        LetSemanticInfo(table)
    } else this

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
            ApplicationName(null, "($operator)"),
            listOf(
                left.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                right.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem)
            ),
            TypeSemanticInfo(
                paramTypePromise()
            ),
            location
        )

        is IfNode -> ApplicationNode(
            ApplicationName("::prelude", "if"),
            listOf(
                condition.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                trueExpression.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                falseExpression.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem)
            ),
            TypeSemanticInfo(
                paramTypePromise()
            ),
            location
        )

        is FunctionCallNode -> if (arguments.isEmpty()) {
            VarNode(
                name,
                info.getVarSemanticInfo(name, location),
                location
            )
        } else {
            val callInfo = info.callSemanticInfo()
            ApplicationNode(
                ApplicationName(name = name),
                arguments.map {
                    it.cast<ExpressionParserNode>().toSemanticNode(errors, callInfo, typeSystem)
                },
                TypeSemanticInfo(
                    paramTypePromise()
                ),
                location
            )
        }

        is UnitNode -> ConstantNode(
            Unit,
            typeSystem.getTypeSemanticInfo("Unit"),
            location
        )

        is LiteralCollectionNode -> {
            val expressions = values.map {
                it.cast<ExpressionParserNode>()
                    .toSemanticNode(errors, info, typeSystem)
            }
            ApplicationNode(
                type.applicationName,
                expressions,
                TypeSemanticInfo(
                    paramTypePromise()
                ),
                location
            )
        }

        is LiteralMapEntryNode -> {
            ApplicationNode(
                ApplicationName("::prelude", "pair"),
                listOf(
                    key.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                    value.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                ),
                TypeSemanticInfo(
                    paramTypePromise()
                ),
                location
            )
        }

        is LetExpressionNode -> {
            val table = SymbolTableBuilder(info.cast<SymbolTable>(), errors)
            val letInfo = LetSemanticInfo(table)
            val bindings = matches.map {
                it.toSemanticNode(errors, letInfo, typeSystem)
                    .cast<LetBindingNode<SemanticInfo>>()
            }
            LetNode(
                bindings,
                expression.cast<ExpressionParserNode>().toSemanticNode(errors, letInfo, typeSystem),
                EmptySemanticInfo,
                location
            )
        }

        is MatchAssignNode -> {
            val letInfo = info.cast<LetSemanticInfo>()
            val matchInfo = MatchSemanticInfo(letInfo.table)
            LetBindingNode(
                matchValue.toSemanticNode(errors, matchInfo, typeSystem),
                expression.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                EmptySemanticInfo,
                location
            )
        }

        is MatchValueNode -> when (type) {
            MatchValueType.Expression -> value.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem)
            else -> TODO()
        }

        else -> TODO("No supported $this")
    }
