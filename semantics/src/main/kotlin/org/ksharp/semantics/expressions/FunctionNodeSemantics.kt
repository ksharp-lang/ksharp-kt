package org.ksharp.semantics.expressions

import inferType
import org.ksharp.common.*
import org.ksharp.module.FunctionInfo
import org.ksharp.module.FunctionVisibility
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.AnnotationNode
import org.ksharp.nodes.ExpressionParserNode
import org.ksharp.nodes.FunctionNode
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.InferenceInfo
import org.ksharp.semantics.nodes.*
import org.ksharp.semantics.scopes.Function
import org.ksharp.semantics.scopes.FunctionTable
import org.ksharp.semantics.scopes.FunctionTableBuilder
import org.ksharp.semantics.scopes.SymbolTableBuilder
import org.ksharp.semantics.typesystem.toAnnotation
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.annotations.Annotation
import org.ksharp.typesystem.types.*

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

private fun Type.typePromise(node: FunctionNode): ErrorOrValue<List<TypePromise>> =
    when (this) {
        is Annotated -> this.type.typePromise(node)
        else -> this.cast<FunctionType>().typePromise(node)
    }

internal fun ModuleNode.buildFunctionTable(
    errors: ErrorCollector,
    typeSystem: TypeSystem
): Pair<FunctionTable, List<FunctionNode>> =
    FunctionTableBuilder(errors).let { table ->
        val listBuilder = listBuilder<FunctionNode>()
        functions.forEach { f ->
            val type = typeSystem["Decl__${f.name}"].valueOrNull
            errors.collect(type.let { it?.typePromise(f) ?: Either.Right(f.typePromise(typeSystem)) })
                .map {
                    val annotations = when (type) {
                        is Annotated -> type.annotations
                        else -> null
                    }
                    table.register(
                        f.name,
                        Function(
                            if (f.pub) FunctionVisibility.Public else FunctionVisibility.Internal,
                            annotations,
                            f.name,
                            it
                        ), f.location
                    ).map { listBuilder.add(f) }
                }
        }
        table.build() to listBuilder.build()
    }

private fun List<AnnotationNode>?.checkAnnotations(): List<Annotation>? =
    this?.map { it.toAnnotation() }

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
            native,
            function.annotations ?: annotations.checkAnnotations(),
            name,
            semanticNode,
            AbstractionSemanticInfo(
                function.visibility,
                parameters.map { symbolTable[it]!!.first }.toList(),
                function.type.last()
            ),
            location
        )
    }

internal fun List<AbstractionNode<SemanticInfo>>.toFunctionInfoMap() =
    this.asSequence().map {
        val semanticInfo = it.info.cast<AbstractionSemanticInfo>()
        val arguments = semanticInfo
            .parameters.map { i ->
                when (val iType = i.getInferredType(it.location)) {
                    is Either.Right -> iType.value
                    else -> newParameter()
                }
            }
        val returnType = semanticInfo.returnType?.getType(it.location)?.valueOrNull
        if (returnType != null) {
            FunctionInfo(it.native, semanticInfo.visibility, null, it.annotations, it.name, arguments + returnType)
        } else FunctionInfo(it.native, semanticInfo.visibility, null, it.annotations, it.name, arguments)
    }.groupBy { it.name }

fun ModuleNode.checkFunctionSemantics(moduleTypeSystemInfo: ModuleTypeSystemInfo): ModuleFunctionInfo {
    val errors = ErrorCollector()
    val (functionTable, functionNodes) = buildFunctionTable(errors, moduleTypeSystemInfo.typeSystem)
    val abstractions = functionNodes
        .asSequence()
        .map {
            it.checkSemantics(errors, functionTable[it.name]!!.first, moduleTypeSystemInfo.typeSystem)
        }
        .filter { it.isRight }
        .map { (it as Either.Right).value }
        .toList()
    return ModuleFunctionInfo(
        errors = errors.build(),
        abstractions = abstractions
    )
}

fun ModuleFunctionInfo.checkInferenceSemantics(
    moduleTypeSystemInfo: ModuleTypeSystemInfo,
    preludeModule: ModuleInfo
): ModuleFunctionInfo {
    val errors = ErrorCollector()
    errors.collectAll(this.errors)
    val inferenceInfo = InferenceInfo(
        preludeModule,
        ModuleInfo(
            emptyList(),
            moduleTypeSystemInfo.typeSystem,
            abstractions.toFunctionInfoMap()
        ),
        emptyMap()
    )
    abstractions.map { it.inferType(inferenceInfo) }
    val abstractions = abstractions.filter {
        val iType = it.info.getInferredType(it.location)
        if (iType.isLeft) {
            errors.collect(iType.cast<Either.Left<Error>>().value)
            false
        } else true
    }
    return ModuleFunctionInfo(
        errors = errors.build(),
        abstractions = abstractions
    )
}
