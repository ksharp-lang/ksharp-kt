package org.ksharp.semantics.expressions

import org.ksharp.common.ErrorCode
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.common.new
import org.ksharp.nodes.*
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.nodes.*
import org.ksharp.semantics.scopes.SymbolTable
import org.ksharp.semantics.scopes.SymbolTableBuilder
import org.ksharp.typesystem.TypeSystem
import kotlin.math.pow

enum class ExpressionSemanticsErrorCode(override val description: String) : ErrorCode {
    SymbolAlreadyUsed("Symbol already used '{name}'")
}

private const val PRELUDE_FLAG = "::prelude"

private val LiteralCollectionType.applicationName
    get(): ApplicationName =
        when (this) {
            LiteralCollectionType.List -> ApplicationName(PRELUDE_FLAG, "listOf")
            LiteralCollectionType.Map -> ApplicationName(PRELUDE_FLAG, "mapOf")
            LiteralCollectionType.Tuple -> ApplicationName(PRELUDE_FLAG, "tupleOf")
            LiteralCollectionType.Set -> ApplicationName(PRELUDE_FLAG, "setOf")
        }

private fun integer2Type(value: Long): String =
    when {
        value < 255 -> "Byte"
        value < 2.0.pow(16.0) -> "Short"
        value < 2.0.pow(32.0) -> "Int"
        else -> "Long"
    }

private fun LiteralValueType.toSemanticInfo(typeSystem: TypeSystem, value: Any) =
    when (this) {
        LiteralValueType.Integer,
        LiteralValueType.HexInteger,
        LiteralValueType.OctalInteger,
        LiteralValueType.BinaryInteger -> {
            typeSystem.getTypeSemanticInfo(integer2Type(value as Long))
        }

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
        table.register(name, paramTypePromise(), location)
        table[name]?.first ?: ExpressionSemanticsErrorCode.SymbolAlreadyUsed.new(location, "name" to name)
            .toTypePromise()
    } else cast<SymbolResolver>().getSymbol(name)
        ?: paramTypePromise()

private fun SemanticInfo.callSemanticInfo(): SemanticInfo =
    if (this is MatchSemanticInfo) {
        LetSemanticInfo(table)
    } else this

private fun String.toApplicationName(): ApplicationName {
    val ix = this.indexOf('.')
    return if (ix != -1) {
        ApplicationName(this.substring(0, ix), this.substring(ix + 1))
    } else ApplicationName(name = this)
}

internal fun ExpressionParserNode.toSemanticNode(
    errors: ErrorCollector,
    info: SemanticInfo,
    typeSystem: TypeSystem
): SemanticNode<SemanticInfo> =
    when (this) {
        is LiteralValueNode -> with(value.toValue(type)) {
            ConstantNode(
                this,
                type.toSemanticInfo(typeSystem, this),
                location
            )
        }

        is OperatorNode -> ApplicationNode(
            ApplicationName(null, "($operator)"),
            listOf(
                left.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                right.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem)
            ),
            EmptySemanticInfo(),
            location
        )

        is IfNode -> ApplicationNode(
            ApplicationName(PRELUDE_FLAG, "if"),
            listOf(
                condition.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                trueExpression.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                falseExpression.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem)
            ),
            EmptySemanticInfo(),
            location
        )

        is FunctionCallNode -> if (arguments.isEmpty()) {
            val varInfo = info.getVarSemanticInfo(name, location)
            if (varInfo is Symbol) {
                VarNode(
                    name,
                    varInfo,
                    location
                )
            } else {
                val callInfo = info.callSemanticInfo()
                ApplicationNode(
                    name.toApplicationName(),
                    listOf(UnitNode(location).toSemanticNode(errors, callInfo, typeSystem)),
                    EmptySemanticInfo(),
                    location
                )
            }
        } else {
            val callInfo = info.callSemanticInfo()
            ApplicationNode(
                name.toApplicationName(),
                arguments.map {
                    it.cast<ExpressionParserNode>().toSemanticNode(errors, callInfo, typeSystem)
                },
                EmptySemanticInfo(),
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
                EmptySemanticInfo(),
                location
            )
        }

        is LiteralMapEntryNode -> {
            ApplicationNode(
                ApplicationName(PRELUDE_FLAG, "pair"),
                listOf(
                    key.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                    value.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                ),
                EmptySemanticInfo(),
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
                EmptySemanticInfo(),
                location
            )
        }

        is MatchAssignNode -> {
            val letInfo = info.cast<LetSemanticInfo>()
            val matchInfo = MatchSemanticInfo(letInfo.table)
            LetBindingNode(
                matchValue.toSemanticNode(errors, matchInfo, typeSystem),
                expression.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                EmptySemanticInfo(),
                location
            )
        }

        is MatchValueNode -> when (type) {
            MatchValueType.Expression -> value.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem)
            else -> TODO()
        }

        else -> TODO("No supported $this")
    }
