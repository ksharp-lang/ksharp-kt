package org.ksharp.semantics.typesystem

import org.ksharp.common.*
import org.ksharp.nodes.*
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.prelude.types.preludeTypeSystem
import org.ksharp.semantics.scopes.ModuleSemanticNode
import org.ksharp.semantics.scopes.Table
import org.ksharp.typesystem.PartialTypeSystem
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.typeSystem
import org.ksharp.typesystem.types.*

enum class TypeSemanticsErrorCode(override val description: String) : ErrorCode {
    UnionTypeArmShouldStartWithName("Union type should start with a name not a parameter. e.g Just a"),
    ParametersNotUsed("Parameters '{params}' not used in the type '{type}'"),
    ParamNameAlreadyDefined("Param '{name}' already defined in type '{type}'"),
    ParamNameNoDefined("Param '{name}' not defined in type '{type}'")
}

private fun parametersNotUsed(name: String, location: Location, params: Sequence<String>) =
    TypeSemanticsErrorCode.ParametersNotUsed.new(
        location,
        "type" to name,
        "params" to params.joinToString(", ")
    )

private val NodeData.params
    get(): Sequence<String> =
        when (this) {
            is ConcreteTypeNode -> emptySequence()
            is ParameterTypeNode -> sequenceOf(this.name)
            else -> this.node.children.map {
                it.cast<NodeData>().params
            }.flatten()
        }

private fun NodeData.checkParams(name: String, location: Location, params: Sequence<String>): ErrorOrValue<Boolean> {
    val paramsSet = mutableSetOf<String>()
    for (param in params) {
        if (paramsSet.contains(param)) {
            return Either.Left(
                TypeSemanticsErrorCode.ParamNameAlreadyDefined.new(
                    location,
                    "name" to param,
                    "type" to name
                )
            )
        } else paramsSet.add(param)
    }
    val nodeParams = this.params.toSet()
    for (param in nodeParams) {
        if (!paramsSet.remove(param))
            return Either.Left(
                TypeSemanticsErrorCode.ParamNameNoDefined.new(
                    location,
                    "name" to param,
                    "type" to name
                )
            )
    }
    if (paramsSet.isNotEmpty()) {
        return Either.Left(
            parametersNotUsed(name, location, paramsSet.asSequence())
        )
    }

    return Either.Right(true)
}

private fun ParametricTypeFactory.register(node: NodeData) =
    when (node) {
        is ConcreteTypeNode -> type(node.name)
        is ParameterTypeNode -> parameter(node.name)
        else -> TODO()
    }

private fun UnionTypeFactory.register(node: NodeData) =
    when (node) {
        is ConcreteTypeNode -> clazz(node.name)
        is ParametricTypeNode -> {
            val variables = node.variables
            val firstNode = variables.first() as NodeData
            if (firstNode is ConcreteTypeNode) {
                clazz(firstNode.name) {
                    variables.asSequence()
                        .drop(1)
                        .forEach { register(it as NodeData) }
                }
            } else error(
                TypeSemanticsErrorCode.UnionTypeArmShouldStartWithName.new(
                    firstNode.location
                )
            )
        }

        is ParameterTypeNode -> error(
            TypeSemanticsErrorCode.UnionTypeArmShouldStartWithName.new(
                node.location
            )
        )

        else -> TODO("$node")
    }

fun TypeItemBuilder.register(node: NodeData) =
    when (node) {
        is ConcreteTypeNode -> type(node.name)

        is TupleTypeNode -> tupleType {
            node.types.forEach {
                register(it as NodeData)
            }
        }

        is UnionTypeNode -> unionType {
            node.types.forEach {
                register(it as NodeData)
            }
        }

        else -> TODO()
    }

private fun TypeSystemBuilder.register(node: TypeNode) =
    alias(node.name, listOf()) {
        this.register(node.expr)
    }

private fun TypeNode.checkSemantics(
    errors: ErrorCollector,
    table: TypeVisibilityTableBuilder,
    builder: TypeSystemBuilder
) = table.register(
    name,
    if (internal) TypeVisibility.Internal else TypeVisibility.Public,
    location
).map {
    errors.collect(expr.checkParams(name, location, params.asSequence())).map {
        builder.register(this)
    }
}

private fun List<TypeNode>.checkSemantics(errors: ErrorCollector): Pair<Table<TypeVisibility>, PartialTypeSystem> {
    val table = TypeVisibilityTableBuilder(errors)
    val typeSystem = typeSystem(preludeTypeSystem) {
        this@checkSemantics.forEach {
            it.checkSemantics(errors, table, this)
        }
    }
    return table.build() to typeSystem
}

fun ModuleNode.checkSemantics(): ModuleSemanticNode {
    val errors = ErrorCollector()
    val (typeTable, typeSystem) = types.checkSemantics(errors)
    errors.collectAll(typeSystem.errors)
    return ModuleSemanticNode(
        errors.build(),
        typeTable,
        typeSystem.value
    )
}
