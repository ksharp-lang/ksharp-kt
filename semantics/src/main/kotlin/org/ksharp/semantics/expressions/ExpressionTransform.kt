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

enum class ExpressionSemanticsErrorCode(override val description: String) : ErrorCode {
    SymbolAlreadyUsed("Symbol already used '{name}'")
}

internal const val PRELUDE_COLLECTION_FLAG = "::collection"

enum class CollectionFunctionName(val applicationName: ApplicationName) {
    List(ApplicationName(PRELUDE_COLLECTION_FLAG, "listOf")),
    Map(ApplicationName(PRELUDE_COLLECTION_FLAG, "mapOf")),
    Tuple(ApplicationName(PRELUDE_COLLECTION_FLAG, "tupleOf")),
    Set(ApplicationName(PRELUDE_COLLECTION_FLAG, "setOf"))
}

private val LiteralCollectionType.applicationName
    get(): ApplicationName =
        when (this) {
            LiteralCollectionType.List -> CollectionFunctionName.List.applicationName
            LiteralCollectionType.Map -> CollectionFunctionName.Map.applicationName
            LiteralCollectionType.Tuple -> CollectionFunctionName.Tuple.applicationName
            LiteralCollectionType.Set -> CollectionFunctionName.Set.applicationName
        }

private fun LiteralValueType.toSemanticInfo(typeSystem: TypeSystem) =
    when (this) {
        LiteralValueType.Integer,
        LiteralValueType.HexInteger,
        LiteralValueType.OctalInteger,
        LiteralValueType.BinaryInteger -> {
            typeSystem.getTypeSemanticInfo("Long")
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

private fun SemanticInfo.callSemanticInfo(fnName: String): SemanticInfo =
    if (this is MatchSemanticInfo) {
        if (fnName.first().isUpperCase()) this
        else LetSemanticInfo(table)
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
        is LiteralValueNode ->
            if (type == LiteralValueType.Binding) {
                val varInfo = info.getVarSemanticInfo(value, location)
                VarNode(
                    value,
                    varInfo,
                    location
                )
            } else with(value.toValue(type)) {
                ConstantNode(
                    this,
                    type.toSemanticInfo(typeSystem),
                    location
                )
            }

        is OperatorNode -> ApplicationNode(
            ApplicationName(null, "($operator)"),
            listOf(
                left.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                right.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem)
            ),
            ApplicationSemanticInfo(),
            location
        )

        is IfNode -> ApplicationNode(
            ApplicationName(null, "if"),
            listOf(
                condition.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                trueExpression.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                falseExpression.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem)
            ),
            ApplicationSemanticInfo(),
            location
        )

        is FunctionCallNode -> if (arguments.isEmpty()) {
            name.variableOrFunctionCallNode(errors, typeSystem, info, location)
        } else {
            val appName = name.toApplicationName()
            val callInfo = info.callSemanticInfo(appName.name)
            ApplicationNode(
                appName,
                arguments.map {
                    it.cast<ExpressionParserNode>().toSemanticNode(errors, callInfo, typeSystem)
                },
                ApplicationSemanticInfo(),
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
                ApplicationSemanticInfo(),
                location
            )
        }

        is LiteralMapEntryNode -> {
            ApplicationNode(
                ApplicationName(null, "pair"),
                listOf(
                    key.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                    value.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                ),
                ApplicationSemanticInfo(),
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

        is MatchExpressionNode -> MatchNode(
            expression.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
            branches.map {
                it.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem)
                    .cast()
            },
            EmptySemanticInfo(),
            location
        )

        is MatchExpressionBranchNode -> {
            val table = SymbolTableBuilder(info.cast<SymbolTable>(), errors)
            val matchInfo = MatchSemanticInfo(table)
            MatchBranchNode(
                match.cast<ExpressionParserNode>().toSemanticNode(errors, matchInfo, typeSystem),
                expression.cast<ExpressionParserNode>().toSemanticNode(errors, LetSemanticInfo(table), typeSystem),
                EmptySemanticInfo(),
                location
            )
        }

        is MatchAssignNode -> {
            val letInfo = info.cast<LetSemanticInfo>()
            val matchInfo = MatchSemanticInfo(letInfo.table)
            LetBindingNode(
                match.cast<ExpressionParserNode>().toSemanticNode(errors, matchInfo, typeSystem),
                expression.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
                EmptySemanticInfo(),
                location
            )
        }

        is MatchListValueNode -> ListMatchValueNode(
            head.map {
                it.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem)
            },
            tail.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
            EmptySemanticInfo(),
            location
        )

        is MatchConditionValueNode -> ConditionalMatchValueNode(
            type,
            left.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
            right.cast<ExpressionParserNode>().toSemanticNode(errors, info, typeSystem),
            EmptySemanticInfo(),
            location
        )
    }

private fun String.variableOrFunctionCallNode(
    errors: ErrorCollector,
    typeSystem: TypeSystem,
    info: SemanticInfo,
    location: Location
): SemanticNode<SemanticInfo> {
    val varInfo = info.getVarSemanticInfo(this, location)
    return if (varInfo is Symbol) {
        VarNode(
            this,
            varInfo,
            location
        )
    } else {
        val appName = this.toApplicationName()
        val callInfo = info.callSemanticInfo(appName.name)
        ApplicationNode(
            appName,
            listOf(UnitNode(location).toSemanticNode(errors, callInfo, typeSystem)),
            ApplicationSemanticInfo(),
            location
        )
    }
}
