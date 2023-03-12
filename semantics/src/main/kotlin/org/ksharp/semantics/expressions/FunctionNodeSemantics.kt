package org.ksharp.semantics.expressions

import org.ksharp.common.*
import org.ksharp.nodes.FunctionNode
import org.ksharp.nodes.ModuleNode
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.MaybePolymorphicTypePromise
import org.ksharp.semantics.inference.ResolvedTypePromise
import org.ksharp.semantics.inference.TypePromise
import org.ksharp.semantics.typesystem.ModuleTypeSystemInfo
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.FunctionType

enum class FunctionSemanticsErrorCode(override val description: String) : ErrorCode {
    WrongNumberOfParameters("Wrong number of parameters for '{name}' respecting their declaration {fnParams} != {declParams}"),
    ParamMismatch("Param mismatch for '{name}'. {fnParam} != {declParam}"),
}

private fun FunctionNode.typePromise(typeSystem: TypeSystem): List<TypePromise> =
    (if (parameters.isEmpty()) {
        listOf(ResolvedTypePromise(typeSystem["Unit"].valueOrNull!!))
    } else parameters.mapIndexed { ix, param ->
        MaybePolymorphicTypePromise(param, "@param_${ix + 1}")
    }) + MaybePolymorphicTypePromise("return", "@param_${parameters.size + 1}")

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
        ResolvedTypePromise(it)
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

private fun FunctionNode.checkSemantics(errors: ErrorCollector, function: Function, typeSystem: TypeSystem) {
    val symbolTable = SymbolTableBuilder(null, errors)
    val typesIter = function.type.iterator()
    println(function)
}

fun ModuleNode.checkFunctionSemantics(moduleTypeSystemInfo: ModuleTypeSystemInfo): ModuleFunctionInfo {
    val errors = ErrorCollector()
    val (functionTable, functionNodes) = buildFunctionTable(errors, moduleTypeSystemInfo.typeSystem)
    functionNodes.map {
        it.checkSemantics(errors, functionTable[it.name]!!.first, moduleTypeSystemInfo.typeSystem)
    }
    return ModuleFunctionInfo(
        errors = errors.build(),
        functionTable = functionTable
    )
}