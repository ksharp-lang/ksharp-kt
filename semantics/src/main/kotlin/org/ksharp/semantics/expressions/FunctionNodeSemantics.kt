package org.ksharp.semantics.expressions

import org.ksharp.common.*
import org.ksharp.nodes.ExpressionParserNode
import org.ksharp.nodes.FunctionNode
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.nodes.*
import org.ksharp.semantics.scopes.*
import org.ksharp.semantics.scopes.Function
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.FunctionType

enum class FunctionSemanticsErrorCode(override val description: String) : ErrorCode {
    WrongNumberOfParameters("Wrong number of parameters for '{name}' respecting their declaration {fnParams} != {declParams}"),
    ParamMismatch("Param mismatch for '{name}'. {fnParam} != {declParam}")
}

private fun FunctionNode.typePromise(typeSystem: TypeSystem): List<TypePromise> =
    (if (parameters.isEmpty()) {
        listOf(TypeSemanticInfo(typeSystem["Unit"]))
    } else parameters.map { _ ->
        paramTypePromise()
    }) + paramTypePromise()

private fun FunctionType.typePromise(node: FunctionNode): ErrorOrValue<List<TypePromise>> {
    val unitParams = node.parameters.isEmpty()
    val parameters = if (unitParams) 2 else node.parameters.size + 1
    if (arguments.size != parameters) {
        return Either.Left(
            FunctionSemanticsErrorCode.WrongNumberOfParameters.new(
                node.location,
                "name" to node.name,
                "fnParams" to parameters,
                "declParams" to arguments.size
            )
        )
    }
    if (unitParams && arguments.first().representation != "Unit") {
        return Either.Left(
            FunctionSemanticsErrorCode.ParamMismatch.new(
                node.location,
                "name" to node.name,
                "fnParam" to "()",
                "declParam" to arguments.first().representation
            )
        )
    }

    return Either.Right(arguments.map {
        TypeSemanticInfo(Either.Right(it))
    })
}


private fun ModuleNode.buildFunctionTable(
    errors: ErrorCollector,
    typeSystem: TypeSystem
): Pair<FunctionTable, List<FunctionNode>> =
    FunctionTableBuilder(errors).let { table ->
        val listBuilder = listBuilder<FunctionNode>()
        functions.forEach { f ->
            errors.collect(typeSystem["Decl__${f.name}"]
                .valueOrNull
                .let { type ->
                    type?.cast<FunctionType>()?.typePromise(f) ?: Either.Right(f.typePromise(typeSystem))
                }).map { type ->
                table.register(
                    f.name,
                    Function(
                        if (f.pub) FunctionVisibility.Public else FunctionVisibility.Internal,
                        f.name,
                        type
                    ), f.location
                ).map { listBuilder.add(f) }
            }
        }
        table.build() to listBuilder.build()
    }

private fun FunctionNode.checkSemantics(
    errors: ErrorCollector,
    function: Function,
    typeSystem: TypeSystem
): Either<Boolean, AbstractionNode<SemanticInfo>> =
    SymbolTableBuilder(null, errors).let { st ->
        val typesIter = function.type.iterator()
        val invalidSymbolTable = Flag()

        for (param in parameters) {
            if (invalidSymbolTable.enabled) break
            st.register(param, typesIter.next(), location).mapLeft {
                invalidSymbolTable.activate()
            }
        }

        if (invalidSymbolTable.enabled) Either.Left(false)
        else Either.Right(st.build())
    }.map { symbolTable ->
        val info = SymbolTableSemanticInfo(symbolTable)
        val semanticNode = expression.cast<ExpressionParserNode>()
            .toSemanticNode(errors, info, typeSystem)
        AbstractionNode(
            name,
            semanticNode,
            AbstractionSemanticInfo(parameters.map { symbolTable[it]!!.first }.toList()),
            location
        )
    }

fun ModuleNode.checkFunctionSemantics(moduleTypeSystemInfo: ModuleTypeSystemInfo): ModuleFunctionInfo {
    val errors = ErrorCollector()
    val (functionTable, functionNodes) = buildFunctionTable(errors, moduleTypeSystemInfo.typeSystem)
    return ModuleFunctionInfo(
        errors = errors.build(),
        functionTable = functionTable,
        abstractions = functionNodes
            .asSequence()
            .map {
                it.checkSemantics(errors, functionTable[it.name]!!.first, moduleTypeSystemInfo.typeSystem)
            }
            .filter { it.isRight }
            .map { (it as Either.Right).value }
            .toList()
    )
}
