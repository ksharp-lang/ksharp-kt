package org.ksharp.compiler

import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.module.CodeModule
import org.ksharp.module.ModuleInfo
import org.ksharp.parser.ksharp.parseModule
import org.ksharp.semantics.nodes.toCodeModule
import java.io.File
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

fun Reader.moduleInfo(context: String, preludeModule: ModuleInfo): Either<List<Error>, CodeModule> =
    this.parseModule(context, true)
        .mapLeft {
            listOf(it.error)
        }.flatMap {
            it.toCodeModule(preludeModule).let { codeModule ->
                if (codeModule.errors.isEmpty()) Either.Right(codeModule)
                else Either.Left(codeModule.errors)
            }
        }

fun String.moduleInfo(context: String, preludeModule: ModuleInfo) =
    this.reader().moduleInfo(context, preludeModule)

fun Path.moduleInfo(preludeModule: ModuleInfo) =
    Files.newBufferedReader(this, StandardCharsets.UTF_8)
        .moduleInfo(fileName.toString(), preludeModule)

fun File.moduleInfo(preludeModule: ModuleInfo) =
    reader(StandardCharsets.UTF_8).moduleInfo(name, preludeModule)
