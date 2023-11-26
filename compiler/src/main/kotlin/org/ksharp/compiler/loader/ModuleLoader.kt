package org.ksharp.compiler.loader

import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.common.ErrorCode
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.bufferView
import org.ksharp.common.new
import org.ksharp.module.FunctionInfo
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.module.bytecode.readModuleInfo
import org.ksharp.parser.ksharp.parseModule
import org.ksharp.semantics.nodes.toCodeModule
import org.ksharp.typesystem.TypeSystem
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader

typealias ErrorsOrModuleInterface = Either<List<Error>, ModuleInterface>

internal fun String.toModulePath(extension: String): String =
    "${this.replace('.', '/')}.$extension"

enum class ModuleLoaderErrorCode(override val description: String) : ErrorCode {
    ModuleNotFound("Module '{name}' not found"),
}

fun interface ModuleExecutable {
    fun execute(name: String, vararg args: Any): Any
}

interface ModuleInterface {
    val name: String
    val dependencies: Map<String, String>
    val typeSystem: TypeSystem
    val functions: Map<String, FunctionInfo>
    val impls: Set<Impl>
    val executable: ModuleExecutable
}

interface SourceLoader {
    fun binaryLoad(path: String): InputStream?
    fun sourceLoad(path: String): Reader?
    fun outputStream(path: String): OutputStream

}

class ModuleLoader(
    private val sources: SourceLoader,
    private val preludeModule: ModuleInfo
) {

    private fun InputStream.readModuleInfo(name: String): ErrorsOrModuleInterface =
        Either.Right(ModuleInfoInterface(name, bufferView(BufferView::readModuleInfo), sources))

    private fun Reader.codeModule(context: String, preludeModule: ModuleInfo): ErrorsOrModuleInterface =
        this.parseModule(context, true)
            .mapLeft {
                listOf(it.error)
            }.flatMap {
                it.toCodeModule(preludeModule).let { codeModule ->
                    if (codeModule.errors.isEmpty())
                        Either.Right(CodeModuleInterface(codeModule, sources).apply {
                            compile()
                        })
                    else Either.Left(codeModule.errors)
                }
            }

    fun load(name: String, from: String): ErrorsOrModuleInterface =
        sources.binaryLoad(name.toModulePath("ksm"))?.readModuleInfo(name)
            ?: name.toModulePath("ks").let {
                sources.sourceLoad(it)?.codeModule(it, preludeModule)
            } ?: Either.Left(listOf(ModuleLoaderErrorCode.ModuleNotFound.new("name" to name)))

}
