package org.ksharp.semantics.typesystem

import org.ksharp.common.*
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.*
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.nodes.ModuleTypeSystemInfo
import org.ksharp.typesystem.*
import org.ksharp.typesystem.annotations.Annotation
import org.ksharp.typesystem.types.*

enum class TypeSemanticsErrorCode(override val description: String) : ErrorCode {
    InvalidUnionArm("Invalid union arm"),
    UnionTypeArmShouldStartWithName("Union type should start with a name not a parameter. e.g Just a"),
    ParametersNotUsed("Parameters '{params}' not used in the type '{type}'"),
    ParamNameAlreadyDefined("Param '{name}' already defined in type '{type}'"),
    ParamNameNoDefined("Param '{name}' not defined in type '{type}'"),
    TypeShouldStartWithName("Type should start with a name not a parameter"),
    TraitShouldHaveJustOneParameter("Trait '{name}' should have just one parameter"),
    ParamNameNoDefinedInMethod("Param '{name}' not defined in method '{type}'"),
    ParamNameAlreadyDefinedInMethod("Param '{name}' already defined in method '{type}'"),
    ParametersNotUsedInMethod("Parameters '{params}' not used in the method '{type}'"),
    TraitWithInvalidMethod("Trait '{name}' has invalid methods"),
    InterceptorTypeWithInvalidType("Interceptor type '{name}' has invalid type arm"),
    ParametricTypeShouldStartWithName("Parametric type should start with a name not a parameter. e.g Num a"),
    FunctionDeclarationShouldBeAFunctionType("Function declaration '{name}' should be a function literal type e.g. sum :: Int -> Int -> Int")
}

private fun parametersNotUsed(name: String, location: Location, params: Sequence<String>) =
    (if (name.first().isUpperCase()) TypeSemanticsErrorCode.ParametersNotUsed
    else TypeSemanticsErrorCode.ParametersNotUsedInMethod).new(
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
                (if (name.first().isUpperCase()) TypeSemanticsErrorCode.ParamNameAlreadyDefined
                else TypeSemanticsErrorCode.ParamNameAlreadyDefinedInMethod).new(
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
                (if (name.first().isUpperCase()) TypeSemanticsErrorCode.ParamNameNoDefined
                else TypeSemanticsErrorCode.ParamNameNoDefinedInMethod).new(
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

private fun ParametricTypeFactory.register(node: NodeData, label: String? = null): Unit =
    when (node) {
        is ConcreteTypeNode -> type(node.name, label)
        is ParameterTypeNode -> parameter(node.name, label)
        is LabelTypeNode -> register(node.expr as NodeData, node.name)
        is ParametricTypeNode -> {
            val variables = node.variables
            val firstNode = variables.first() as NodeData
            if (firstNode is ConcreteTypeNode) {
                parametricType(firstNode.name, label) {
                    variables.asSequence()
                        .drop(1)
                        .forEach { register(it as NodeData) }
                }
            } else error(
                TypeSemanticsErrorCode.ParametricTypeShouldStartWithName.new(
                    firstNode.location
                )
            )
        }

        is TupleTypeNode -> tupleType(label) {
            node.types.forEach {
                register(it as NodeData)
            }
        }

        is UnitTypeNode -> type("Unit", label)

        is FunctionTypeNode -> functionType(label) {
            node.params.forEach {
                register(it as NodeData)
            }
        }

        else -> TODO("$node")
    }

private fun UnionTypeFactory.register(node: NodeData) {
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

        else -> error(
            TypeSemanticsErrorCode.InvalidUnionArm.new(
                node.location
            )
        )
    }
}

fun TypeItemBuilder.register(name: String, node: NodeData): ErrorOrType =
    when (node) {
        is ConcreteTypeNode -> type(node.name)
        is UnitTypeNode -> type("Unit")

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

        is ParametricTypeNode -> {
            val params = node.variables
            val firstNode = params.first() as NodeData
            if (firstNode is ConcreteTypeNode) {
                parametricType(firstNode.name) {
                    params.asSequence()
                        .drop(1)
                        .forEach { register(it as NodeData) }
                }
            } else TypeSemanticsErrorCode.TypeShouldStartWithName.new(
                firstNode.location
            ).let { Either.Left(it) }
        }

        is ParameterTypeNode -> TypeSemanticsErrorCode.TypeShouldStartWithName.new(
            node.location
        ).let { Either.Left(it) }

        is FunctionTypeNode -> functionType {
            node.params.forEach {
                register(it as NodeData)
            }
        }

        is IntersectionTypeNode -> intersectionType {
            node.types.forEach {
                when (it) {
                    is ConcreteTypeNode -> type(it.name)
                    else -> {
                        error(TypeSemanticsErrorCode.InterceptorTypeWithInvalidType.new(node.location, "name" to name))
                    }
                }
            }
        }

        else -> TODO("$node")
    }

fun AnnotationNode.toAnnotation(): Annotation =
    Annotation(
        name,
        attrs.entries.associate {
            val value = it.value
            it.key to if (value is AnnotationNode) value.toAnnotation() else value
        }
    )

private fun List<AnnotationNode>?.checkAnnotations(): List<Annotation> =
    this?.map { it.toAnnotation() } ?: emptyList()

private fun TypeSystemBuilder.register(node: TypeNode) =
    alias(
        if (node.internal) TypeVisibility.Internal else TypeVisibility.Public,
        node.name,
        node.annotations.checkAnnotations()
    ) {
        this.register(node.name, node.expr)
    }

private fun TypeNode.checkTypesSemantics(
    errors: ErrorCollector,
    builder: TypeSystemBuilder
) = errors.collect(expr.checkParams(name, location, params.asSequence())).map {
    builder.register(this)
}

private fun TraitNode.checkTypesSemantics(
    errors: ErrorCollector,
    builder: TypeSystemBuilder
) = (if (params.size != 1) Either.Left(
    TypeSemanticsErrorCode.TraitShouldHaveJustOneParameter.new(location, "name" to name)
) else Either.Right(true))
    .let { errors.collect(it) }
    .map {
        builder.trait(
            if (internal) TypeVisibility.Internal else TypeVisibility.Public,
            name,
            params.first(),
            this.annotations.checkAnnotations()
        ) {
            definition.functions.forEach { f ->
                errors.collect(f.checkParams(f.name, f.location, params.asSequence()))
                    .let { paramsCheckResult ->
                        paramsCheckResult.mapLeft {
                            error(TypeSemanticsErrorCode.TraitWithInvalidMethod.new(location, "name" to name))
                        }
                        paramsCheckResult.map {
                            method(f.name) {
                                f.type
                                    .cast<FunctionTypeNode>()
                                    .params
                                    .forEach {
                                        register(it as NodeData)
                                    }
                            }
                        }
                    }
            }
        }
    }

private fun TypeDeclarationNode.checkTypesSemantics(
    errors: ErrorCollector,
    builder: TypeSystemBuilder
) = errors.collect(type.cast<NodeData>().checkParams(name, location, params.asSequence())).map {
    if (type !is FunctionTypeNode)
        errors.collect(
            TypeSemanticsErrorCode
                .FunctionDeclarationShouldBeAFunctionType
                .new(location, "name" to name)
        )
    else builder.alias(TypeVisibility.Internal, "Decl__$name", annotations.checkAnnotations()) {
        this.register(name, type.cast())
    }
}

private fun Sequence<NodeData>.checkTypesSemantics(errors: ErrorCollector): PartialTypeSystem {
    val typeSystem = typeSystem(PartialTypeSystem(preludeModule.typeSystem, listOf())) {
        this@checkTypesSemantics.forEach {
            when (it) {
                is TypeNode -> it.checkTypesSemantics(errors, this)
                is TraitNode -> it.checkTypesSemantics(errors, this)
                is TypeDeclarationNode -> it.checkTypesSemantics(errors, this)
                else -> TODO("$it")
            }
        }
    }
    return typeSystem
}

fun ModuleNode.checkTypesSemantics(): ModuleTypeSystemInfo {
    val errors = ErrorCollector()
    val typeSystem = sequenceOf(
        types.asSequence(),
        typeDeclarations.asSequence()
    ).flatten().checkTypesSemantics(errors)
    errors.collectAll(typeSystem.errors)
    return ModuleTypeSystemInfo(
        errors.build(),
        typeSystem.value
    )
}
