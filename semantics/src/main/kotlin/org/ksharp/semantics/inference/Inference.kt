import org.ksharp.common.*
import org.ksharp.module.FunctionInfo
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.semantics.nodes.type
import org.ksharp.semantics.nodes.types
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.Type

enum class InferenceErrorCode(override val description: String) : ErrorCode {
    FunctionNotFound("Function '{function}' not found")
}

data class InferenceInfo(
    val prelude: ModuleInfo,
    val module: ModuleInfo,
    val dependencies: Map<String, ModuleInfo> = emptyMap()
) {
    private val cache = cacheOf<Pair<String, List<Type>>, Either<String, FunctionInfo>>()

    private fun ModuleInfo.findFunction(name: String, numParams: Int): Sequence<FunctionInfo>? =
        functions.asSequence().find {
            it.key == name
        }?.value?.asSequence()
            ?.filter {
                it.types.size == numParams
            }

    private fun functionName(name: String, arguments: List<Type>) =
        "$name ${
            arguments.joinToString(" ") {
                it.representation
            }
        }"

    fun findFunction(location: Location, appName: ApplicationName, arguments: List<Type>): ErrorOrValue<FunctionInfo> =
        arguments.size.let { numArguments ->
            println(numArguments)
            val name = appName.name
            cache.get(name to arguments) {
                val functions = module.findFunction(name, numArguments)
                    ?: prelude.findFunction(name, numArguments)
                    ?: return@get Either.Left(functionName(name, arguments))

                functions.firstOrNull()?.let {
                    Either.Right(it)
                } ?: Either.Left(functionName(name, arguments))
            }.mapLeft {
                InferenceErrorCode.FunctionNotFound.new(location, "function" to it)
            }
        }
}

private val SemanticNode<SemanticInfo>.inferFlag: Flag by lazy { Flag() }
val SemanticNode<SemanticInfo>.infered: Boolean get() = this.inferFlag.enabled
fun SemanticNode<SemanticInfo>.inferType(info: InferenceInfo): ErrorOrType =
    when (this) {
        is AbstractionNode -> infer(info)
        is ApplicationNode -> infer(info)
        is ConstantNode -> infer()
        is VarNode -> TODO()
        is LetNode -> TODO()
        is LetBindingNode -> TODO()
    }.also {
        inferFlag.activate()
    }

private fun AbstractionNode<SemanticInfo>.infer(info: InferenceInfo): ErrorOrType =
    expression.inferType(info)

private fun ConstantNode<SemanticInfo>.infer(): ErrorOrType =
    info.type

private fun ApplicationNode<SemanticInfo>.infer(info: InferenceInfo): ErrorOrType =
    arguments.asSequence().map { it.info }.types.map {
        info.findFunction(location, functionName, it)
        TODO()
    }
