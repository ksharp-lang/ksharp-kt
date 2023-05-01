import org.ksharp.common.*
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.inference.InferenceInfo
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.semantics.nodes.getType
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.Type

enum class InferenceErrorCode(override val description: String) : ErrorCode {
    TypeNotInferred("Type not inferred"),
    FunctionNotFound("Function '{function}' not found")
}

private fun Sequence<ErrorOrType>.collect(): ErrorOrValue<List<Type>> = run {
    val builder = listBuilder<Type>()
    for (info in this) {
        if (info.isLeft) return@run info.cast<ErrorOrValue<List<Type>>>()
        else builder.add(info.cast<Either.Right<Type>>().value)
    }
    Either.Right(builder.build())
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
            is LetNode -> TODO()
            is LetBindingNode -> TODO()
        }.also { this.info.setInferredType(it) }
    }

private fun AbstractionNode<SemanticInfo>.infer(info: InferenceInfo): ErrorOrType =
    expression.inferType(info)

private fun ConstantNode<SemanticInfo>.infer(): ErrorOrType =
    info.getType(location)

private fun VarNode<SemanticInfo>.infer(): ErrorOrType =
    info.getType(location)

private fun ApplicationNode<SemanticInfo>.infer(info: InferenceInfo): ErrorOrType =
    arguments.asSequence()
        .map { it.inferType(info) }
        .collect()
        .flatMap {
            info.findFunction(location, functionName, it)
        }

