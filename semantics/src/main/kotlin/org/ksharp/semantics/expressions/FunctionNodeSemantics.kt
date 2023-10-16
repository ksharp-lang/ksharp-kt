package org.ksharp.semantics.expressions

import org.ksharp.common.*
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.ExpressionParserNode
import org.ksharp.nodes.FunctionNode
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.context.SemanticContext
import org.ksharp.semantics.context.TraitSemanticContext
import org.ksharp.semantics.context.TypeSystemSemanticContext
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.ConcreteModuleInfo
import org.ksharp.semantics.inference.InferenceInfo
import org.ksharp.semantics.inference.inferType
import org.ksharp.semantics.inference.toSemanticModuleInfo
import org.ksharp.semantics.nodes.*
import org.ksharp.semantics.scopes.Function
import org.ksharp.semantics.scopes.FunctionTable
import org.ksharp.semantics.scopes.FunctionTableBuilder
import org.ksharp.semantics.scopes.SymbolTableBuilder
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.TraitType

enum class FunctionSemanticsErrorCode(override val description: String) : ErrorCode {
    WrongNumberOfParameters("Wrong number of parameters for '{name}' respecting their declaration {fnParams} != {declParams}"),
    ParamMismatch("Param mismatch for '{name}'. {fnParam} != {declParam}"),
    InvalidFunctionName("Function name can't contain '.' symbol '{name}'")
}

private fun FunctionNode.typePromise(typeSystem: TypeSystem): List<TypePromise> =
    (if (parameters.isEmpty()) {
        listOf(TypeSemanticInfo(typeSystem["Unit"]))
    } else parameters.map { _ ->
        typeSystem.paramTypePromise()
    }) + typeSystem.paramTypePromise()

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

internal fun String.checkFunctionName(location: Location): ErrorOrValue<Unit> {
    val ix = this.indexOf(".")
    return if (ix == -1) Either.Right(Unit)
    else Either.Left(FunctionSemanticsErrorCode.InvalidFunctionName.new(location, "name" to this))
}

val FunctionNode.nameWithArity: String
    get() = "$name/${parameters.size + 1}"

internal fun List<FunctionNode>.buildFunctionTable(
    errors: ErrorCollector,
    context: SemanticContext
): Pair<FunctionTable, List<FunctionNode>> =
    FunctionTableBuilder(errors).let { table ->
        val listBuilder = listBuilder<FunctionNode>()
        this.forEach { f ->
            errors.collect(f.name.checkFunctionName(f.location)).map {
                val type = context.findFunctionType(f.nameWithArity)
                errors.collect(type?.typePromise(f) ?: Either.Right(f.typePromise(context.typeSystem)))
                    .map {
                        val visibility = context.calculateVisibility(f)
                        val attributes = if (type != null) {
                            mutableSetOf<Attribute>().apply {
                                addAll(type.attributes)
                                remove(CommonAttribute.Public)
                                remove(CommonAttribute.Internal)
                                add(visibility)
                            }
                        } else setOf<Attribute>(visibility)
                        table.register(
                            f.nameWithArity,
                            Function(
                                attributes,
                                f.name,
                                it
                            ), f.location
                        ).map { listBuilder.add(f) }
                    }
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
        val attributes = mutableSetOf<Attribute>()
        if (native) attributes.add(CommonAttribute.Native)
        attributes.addAll(function.attributes)
        attributes.addAll(annotations.toAttributes(NoAttributes))
        AbstractionNode(
            attributes,
            name,
            semanticNode,
            AbstractionSemanticInfo(
                if (parameters.isEmpty()) {
                    emptyList()
                } else parameters.map { symbolTable[it]!!.first }.toList(),
                function.type.last()
            ),
            location
        )
    }

private fun List<FunctionNode>.checkFunctionSemantics(
    errors: ErrorCollector,
    context: SemanticContext
): List<AbstractionNode<SemanticInfo>> {
    val (functionTable, functionNodes) = buildFunctionTable(errors, context)
    val abstractions = functionNodes
        .asSequence()
        .map {
            it.checkSemantics(errors, functionTable[it.nameWithArity]!!.first, context.typeSystem)
        }
        .filter { it.isRight }
        .map { (it as Either.Right).value }
        .toList()
    return abstractions
}

fun ModuleNode.checkFunctionSemantics(moduleTypeSystemInfo: ModuleTypeSystemInfo): ModuleFunctionInfo {
    val errors = ErrorCollector()
    val typeSystem = moduleTypeSystemInfo.typeSystem
    val moduleContext = TypeSystemSemanticContext(typeSystem)
    val functionAbstractions = functions.checkFunctionSemantics(errors, moduleContext)
    val traitAbstractions = traits.asSequence()
        .filter { it.definition.functions.isNotEmpty() }
        .mapNotNull {
            val traitType = typeSystem[it.name].valueOrNull as? TraitType
            if (traitType != null) {
                it to traitType
            } else null
        }.map {
            val traitContext = TraitSemanticContext(typeSystem, it.second)
            val trait = it.first
            trait.name to trait.definition.functions.checkFunctionSemantics(errors, traitContext)
        }.filter {
            it.second.isNotEmpty()
        }.toMap()
    val impls = moduleTypeSystemInfo.impls
    val implAbstractions = this.impls.asSequence()
        .filter { impls.contains(Impl(it.traitName, it.forName)) }
        .map {
            val traitContext = TraitSemanticContext(typeSystem, typeSystem[it.traitName].valueOrNull!!.cast())
            Impl(it.traitName, it.forName) to it.functions.checkFunctionSemantics(errors, traitContext)
        }.toMap()
    return ModuleFunctionInfo(
        errors.build(),
        functionAbstractions,
        traitAbstractions,
        implAbstractions
    )
}

fun ModuleFunctionInfo.checkInferenceSemantics(
    moduleTypeSystemInfo: ModuleTypeSystemInfo,
    preludeModule: ModuleInfo
): ModuleFunctionInfo {
    val errors = ErrorCollector()
    errors.collectAll(this.errors)
    val inferenceInfo = InferenceInfo(
        ConcreteModuleInfo(preludeModule),
        abstractions.toSemanticModuleInfo(moduleTypeSystemInfo.typeSystem, moduleTypeSystemInfo.impls),
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
        abstractions,
        traitsAbstractions, //TODO: build the traits for the module
        implAbstractions, //TODO: build the impls for the module
    )
}
