import org.ksharp.common.*
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.inference.InferenceInfo
import org.ksharp.semantics.nodes.AbstractionSemanticInfo
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.semantics.nodes.getType
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.toFunctionType

enum class InferenceErrorCode(override val description: String) : ErrorCode {
    TypeNotInferred("Type not inferred"),
    FunctionNotFound("Function '{function}' not found")
}

fun SemanticNode<SemanticInfo>.inferType(info: InferenceInfo): ErrorOrType =
    if (this.info.hasInferredType()) {
        this.info.getInferredType(location)
    } else {
        when (this) {
            is AbstractionNode -> infer(info)
            is ApplicationNode -> infer(info)
            is ConstantNode -> infer()
            is VarNode -> infer()
            is LetNode -> infer(info)
            is LetBindingNode -> infer(info)
        }.also { this.info.setInferredType(it) }
    }

private fun LetBindingNode<SemanticInfo>.infer(info: InferenceInfo): ErrorOrType =
    expression.inferType(info).map {
        val type = if (it is FunctionType) {
            it.arguments.last()
        } else it
        match.info.setInferredType(Either.Right(type))
        type
    }

private fun LetNode<SemanticInfo>.infer(info: InferenceInfo): ErrorOrType =
    this.bindings.map {
        it.inferType(info)
    }.firstOrNull { it.isLeft }
        ?: expression.inferType(info)


private fun AbstractionNode<SemanticInfo>.infer(info: InferenceInfo): ErrorOrType =
    expression.inferType(info).flatMap { returnType ->
        val params = this.info.cast<AbstractionSemanticInfo>().parameters
        if (params.isEmpty()) {
            info.prelude.typeSystem["Unit"].map { unitType ->
                listOf(unitType, returnType).toFunctionType()
            }
        } else {
            params.asSequence()
                .map { it.getInferredType(location) }
                .unwrap()
                .map {
                    (it + returnType).toFunctionType()
                }
        }
    }

private fun ConstantNode<SemanticInfo>.infer(): ErrorOrType =
    info.getType(location)

private fun VarNode<SemanticInfo>.infer(): ErrorOrType =
    info.getType(location)

private fun ApplicationNode<SemanticInfo>.infer(info: InferenceInfo): ErrorOrType =
    arguments.asSequence()
        .map { it.inferType(info) }
        .unwrap()
        .flatMap {
            info.findFunction(location, functionName, it).map { fn ->
                val inferredFn = fn.cast<FunctionType>()
                inferredFn.arguments.asSequence()
                    .zip(arguments.asSequence()) { fnArg, arg ->
                        arg.info.setInferredType(Either.Right(fnArg))
                    }.last()
                inferredFn.arguments.last()
            }
        }