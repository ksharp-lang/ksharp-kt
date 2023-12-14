package org.ksharp.semantics.expressions

import org.ksharp.common.*
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.ExpressionParserNode
import org.ksharp.nodes.FunctionNode
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.context.ImplSemanticContext
import org.ksharp.semantics.context.SemanticContext
import org.ksharp.semantics.context.TraitSemanticContext
import org.ksharp.semantics.context.TypeSystemSemanticContext
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.*
import org.ksharp.semantics.nodes.ModuleFunctionInfo
import org.ksharp.semantics.nodes.ModuleTypeSystemInfo
import org.ksharp.semantics.nodes.SemanticModuleInterface
import org.ksharp.semantics.nodes.paramTypePromise
import org.ksharp.semantics.scopes.Function
import org.ksharp.semantics.scopes.FunctionTable
import org.ksharp.semantics.scopes.FunctionTableBuilder
import org.ksharp.semantics.scopes.SymbolTableBuilder
import org.ksharp.semantics.typesystem.TypeSemanticsErrorCode
import org.ksharp.semantics.typesystem.nameWithArity
import org.ksharp.semantics.typesystem.type
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
    get() = "$name/${parameters.size}"

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

fun ModuleNode.checkFunctionSemantics(
    moduleTypeSystemInfo: ModuleTypeSystemInfo,
    dependencies: Map<String, ModuleInfo> = mapOf()
): ModuleFunctionInfo {
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
    val unificationChecker = unificationChecker(moduleTypeSystemInfo.traitFinderContext)
    val implAbstractions = moduleTypeSystemInfo.impls.asSequence()
        .map {
            val traitContext =
                ImplSemanticContext(
                    typeSystem,
                    it.value.location,
                    it.key.type,
                    it.key.trait.type(typeSystem, dependencies).valueOrNull!!.cast(),
                    unificationChecker
                )
            it.key to it.value.functions.checkFunctionSemantics(errors, traitContext)
        }.toMap()
    return ModuleFunctionInfo(
        errors.build(),
        functionAbstractions,
        traitAbstractions,
        implAbstractions
    )
}

private fun List<AbstractionNode<SemanticInfo>>.inferTypes(
    errors: ErrorCollector,
    info: InferenceInfo
): List<AbstractionNode<SemanticInfo>> {
    map { abstraction -> abstraction.inferType("", info) }
    return filter {
        val iType = it.info.getInferredType(it.location)
        if (iType.isLeft) {
            errors.collect(iType.cast<Either.Left<Error>>().value)
            false
        } else true
    }
}

private fun List<AbstractionNode<SemanticInfo>>.checkFunctionSemantics(
    impl: Impl,
    type: TraitType,
    errors: ErrorCollector
) {
    val requiredMethodsToImplement = type
        .methods
        .values
        .filter { !it.withDefaultImpl }
        .map(TraitType.MethodType::nameWithArity).toSet()

    val collector = mutableSetOf<String>()

    forEach {
        val methodArity = it.nameWithArity
        if (!collector.add(methodArity))
            errors.collect(
                TypeSemanticsErrorCode.DuplicateImplMethod.new(it.location, "name" to methodArity)
            )
    }

    val missingMethods = requiredMethodsToImplement.minus(collector.toSet())
    if (missingMethods.isNotEmpty()) {
        errors.collect(
            TypeSemanticsErrorCode.MissingImplMethods.new(
                "methods" to missingMethods.joinToString(", "),
                "impl" to impl.type.representation,
                "trait" to impl.trait
            )
        )
    }
}

fun SemanticModuleInterface.checkInferenceSemantics(): ModuleFunctionInfo {
    val errors = ErrorCollector()

    val preludeInferenceContext = ModuleInfoInferenceContext(preludeModule)
    val moduleInferenceContext = functionInfo.abstractions.toInferenceContext(
        typeSystemInfo.typeSystem, typeSystemInfo.impls.keys
    )

    val moduleDependencies = dependencies
    val dependencies = dependencies.mapValues {
        ModuleInfoInferenceContext(it.value)
    }

    val abstractionsInferenceInfo = InferenceInfo(
        preludeInferenceContext,
        moduleInferenceContext,
        dependencies
    )

    val abstractions = functionInfo.abstractions.inferTypes(errors, abstractionsInferenceInfo)
    val traitsAbstractions = functionInfo.traitsAbstractions.asSequence().associate { trait ->
        val traitInferenceInfo = InferenceInfo(
            preludeInferenceContext,
            trait.value.toTraitInferenceContext(
                moduleInferenceContext,
                typeSystemInfo.typeSystem[trait.key].valueOrNull!!.cast()
            ),
            dependencies
        )
        trait.key to trait.value.inferTypes(errors, traitInferenceInfo)
    }
    val implAbstractions = functionInfo.implAbstractions.asSequence().associate { impl ->
        val traitType = impl.key.trait.type(typeSystemInfo.typeSystem, moduleDependencies)
        val implInferenceInfo = InferenceInfo(
            preludeInferenceContext,
            impl.value.toImplInferenceContext(
                moduleInferenceContext,
                traitType.valueOrNull!!.cast()
            ),
            dependencies
        )
        val result = impl.value.inferTypes(errors, implInferenceInfo)
        traitType
            .map { t ->
                result.checkFunctionSemantics(impl.key, t.cast(), errors)
            }

        impl.key to result
    }
    return ModuleFunctionInfo(
        errors = errors.build(),
        abstractions,
        traitsAbstractions,
        implAbstractions
    )
}
