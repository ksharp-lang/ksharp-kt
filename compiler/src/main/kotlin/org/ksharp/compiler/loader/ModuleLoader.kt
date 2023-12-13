package org.ksharp.compiler.loader

import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.common.ErrorCode
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.bufferView
import org.ksharp.common.new
import org.ksharp.ir.serializer.readIrModule
import org.ksharp.ir.serializer.writeTo
import org.ksharp.ir.toIrModule
import org.ksharp.module.ModuleInfo
import org.ksharp.module.bytecode.readModuleInfo
import org.ksharp.module.bytecode.writeTo
import org.ksharp.parser.ksharp.parseModule
import org.ksharp.semantics.nodes.ModuleInfoLoader
import org.ksharp.semantics.nodes.toCodeModule
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader

typealias ErrorsOrModule = Either<List<Error>, Module>

internal fun String.toModulePath(extension: String): String =
    "${this.replace('.', '/')}.$extension"

enum class ModuleLoaderErrorCode(override val description: String) : ErrorCode {
    ModuleNotFound("Module '{name}' not found"),
}

fun interface ModuleExecutable {
    fun execute(name: String, vararg args: Any): Any
}

class Module(
    val name: String,
    val info: ModuleInfo,
    private val sources: SourceLoader
) {
    val executable: ModuleExecutable by lazy {
        sources.binaryLoad(name.toModulePath("ksc"))!!
            .bufferView {
                IrModuleExecutable(it.readIrModule())
            }
    }
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

    private fun InputStream.readModuleInfo(name: String): ErrorsOrModule =
        Either.Right(Module(name, bufferView(BufferView::readModuleInfo), sources))

    private fun Reader.codeModule(context: String, preludeModule: ModuleInfo): ErrorsOrModule =
        this.parseModule(context, true)
            .mapLeft {
                listOf(it.error)
            }.flatMap {
                it.toCodeModule(preludeModule, moduleInfoLoader).let { codeModule ->
                    if (codeModule.errors.isEmpty()) {
                        sources.outputStream(codeModule.name.toModulePath("ksm")).let { stream ->
                            codeModule.module.writeTo(stream)
                        }
                        sources.outputStream(codeModule.name.toModulePath("ksc")).let { stream ->
                            codeModule.toIrModule().writeTo(stream)
                        }
                        Either.Right(Module(codeModule.name, codeModule.module, sources))
                    } else Either.Left(codeModule.errors)
                }
            }

    fun load(name: String, from: String): ErrorsOrModule =
        sources.binaryLoad(name.toModulePath("ksm"))?.readModuleInfo(name)
            ?: name.toModulePath("ks").let {
                sources.sourceLoad(it)?.codeModule(it, preludeModule)
            } ?: Either.Left(listOf(ModuleLoaderErrorCode.ModuleNotFound.new("name" to name)))

}

val ModuleLoader.moduleInfoLoader: ModuleInfoLoader
    get() = ModuleInfoLoader { name, from ->
        load(name, from).valueOrNull?.info
    }
