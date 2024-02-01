package org.ksharp.compiler.loader

import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.common.ErrorCode
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.bufferView
import org.ksharp.common.new
import org.ksharp.doc.DocModule
import org.ksharp.doc.readDocModule
import org.ksharp.doc.toDocModule
import org.ksharp.doc.writeTo
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
    CyclingReference("Cycling reference loading '{module}' from '{from}'")
}

fun interface ModuleExecutable {
    fun execute(name: String, vararg args: Any): Any
}

class Module(
    val name: String,
    val info: ModuleInfo,
    private val sources: SourceLoader
) {
    val documentation: DocModule by lazy {
        sources.binaryLoad(name.toModulePath("ksd"))!!
            .bufferView {
                it.readDocModule()
            }
    }

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
    fun outputStream(path: String, action: (OutputStream) -> Unit)
}

class ModuleLoader(
    private val sources: SourceLoader,
    private val preludeModule: ModuleInfo
) {
    private val cyclingRefs = CyclingReferences()

    private fun InputStream.readModuleInfo(name: String): ErrorsOrModule =
        Either.Right(Module(name, bufferView(BufferView::readModuleInfo), sources))

    private fun SourceLoader.write(path: String, action: (stream: OutputStream) -> Unit) {
        outputStream(path, action)
    }

    private fun Reader.codeModule(context: String, preludeModule: ModuleInfo): ErrorsOrModule =
        this.parseModule(context, true)
            .mapLeft {
                listOf(it.error)
            }.flatMap {
                it.toCodeModule(preludeModule, moduleInfoLoader).let { codeModule ->
                    if (codeModule.errors.isEmpty()) {
                        sources.write(codeModule.name.toModulePath("ksd")) { stream ->
                            it.toDocModule(codeModule.module)
                                .writeTo(stream)
                        }
                        sources.write(codeModule.name.toModulePath("ksm")) { stream ->
                            codeModule.module.writeTo(stream)
                        }
                        sources.write(codeModule.name.toModulePath("ksc")) { stream ->
                            codeModule.toIrModule().writeTo(stream)
                        }
                        Either.Right(Module(codeModule.name, codeModule.module, sources))
                    } else Either.Left(codeModule.errors)
                }
            }

    fun load(name: String, from: String): ErrorsOrModule =
        sources.binaryLoad(name.toModulePath("ksm"))?.readModuleInfo(name)
            ?: name.toModulePath("ks").let {
                val dependencies = cyclingRefs.loading(name, from)
                if (dependencies.isEmpty()) {
                    sources.sourceLoad(it)
                        ?.codeModule(it.substring(0, it.length - 3), preludeModule)
                        ?.map { module ->
                            cyclingRefs.loaded(name)
                            module
                        }
                } else Either.Left(
                    listOf(
                        ModuleLoaderErrorCode.CyclingReference.new(
                            "module" to name,
                            "from" to dependencies.joinToString(", ")
                        )
                    )
                )
            } ?: Either.Left(listOf(ModuleLoaderErrorCode.ModuleNotFound.new("name" to name)))

}

val ModuleLoader.moduleInfoLoader: ModuleInfoLoader
    get() = ModuleInfoLoader { name, from ->
        load(name, from).valueOrNull?.info
    }
